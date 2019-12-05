package com.github.pandora.asyncResult;


import com.github.pandora.listenable.executor.RunNowExecutor;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 代表异步结果。当前future所有操作，都不会产生阻塞。
 *
 * 1. {@link #map(Function),#map(Function, Executor)}对异步结果进行转换，当且仅当异步结果正常时才执行。相当于reactive#map操作符
 * 2. {@link #flatMap(Function),#map(Function, Executor)}对异步结果转换另一种异步结果，当且仅当异步结果正常时才执行。参考reactive#flatMap操作符
 * 3. {@link #otherwise(Function)}对异常的异步结果转换成正常的异步结果，当且仅当结果异常时才执行。相当于reactive#doOnError操作符
 * 4. 以上所有的参考都已经用try catch包住了，操作符处理，操作符与操作符之间可以不处理异常，通过在{@link #addHandler(Handler)}统一
 *    处理异常。
 * 5. {@link #addHandler(Handler)}对异步结果处理。相当于reactive#subscribe操作符
 *
 * {@link java.util.concurrent.Future}是阻塞式api。
 *
 * note: 使用{@link #result(),#cause()}获取异步结果时，务必使用{@link #succeeded(),#failed()}判断当前异步结果的状态，
 *       否则可能发生类型转换异常。
 *
 *
 * created by wang007 on 2019/12/1
 */
public interface Future<T> extends AsyncResult<T>, Asyncable<T> {

    /**
     * 代表当前future是否已完成
     *
     * @return true: 当前future已完成，false: 未完成
     */
    boolean isCompleted();

    /**
     * 添加异步回调处理器，可以添加多个。
     *
     * @param handler 异步回调处理器
     * @return this
     */
    Future<T> addHandler(Handler<AsyncResult<T>> handler);

    /**
     * 将类型T转换成类型R.
     * <p>
     * 操作符fn的异常统一抓起来，在最后时统一使用{@link #addHandler(Handler)}处理。异常可在操作符上流通。
     * <p>
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenApply}.
     *
     * @param fn  转换函数
     * @param <R> 类型R
     * @return 新的、可链式的Future
     */
    @Override
    default <R> Future<R> map(Function<? super T, ? extends R> fn) {
        return map(fn, RunNowExecutor.Executor);
    }

    /**
     * 将类型T转换成类型R.
     * <p>
     * 操作符fn的异常统一抓起来，在最后时统一使用{@link #addHandler(Handler)}处理。异常可在操作符上流通。
     * <p>
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenApplyAsync(Function, Executor)}.
     *
     * @param fn       转换函数
     * @param executor 将fn执行在声明的executor上。 executor == null，throw NPE
     * @param <R>      类型R
     * @return 新的、可链式的Future
     */
    default <R> Future<R> map(Function<? super T, ? extends R> fn, Executor executor) {
        Objects.requireNonNull(fn);
        Objects.requireNonNull(executor);
        Promise<R> promise = Promise.promise();
        addHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    executor.execute(() -> {
                        try {
                            R apply = fn.apply(ar.result());
                            promise.setSuccess(apply);
                        } catch (Throwable e) {
                            promise.setFailure(e);
                        }
                    });
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            } else {
                promise.setFailure(ar.cause());
            }
        });
        return promise.toFuture();
    }

    /**
     * 将包含类型T的future，转换成包含类型R的Future.
     * <p>
     * 此操作符可平铺callback，解决callback hell
     * <p>
     * 操作符fn的异常统一抓起来，在最后时统一使用{@link #addHandler(Handler)}处理。异常可在操作符上流通。
     * <p>
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenCompose}.
     *
     * @param fn  continuation
     * @param <R> 类型R
     * @return 新的、可链式的Future
     */
    default <R> Future<R> flatMap(Function<? super T, ? extends Asyncable<? extends R>> fn) {
        return flatMap(fn, RunNowExecutor.Executor);
    }

    /**
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenComposeAsync(Function, Executor)}.
     * <p>
     * 此操作符可平铺callback，解决callback hell
     *
     * @param fn       continuation
     * @param executor 将fn执行在声明的executor上。 executor == null，throw NPE
     * @param <R>      类型R
     * @return 新的、可链式的Future
     * @see #flatMap(Function)
     */
    default <R> Future<R> flatMap(Function<? super T, ? extends Asyncable<? extends R>> fn, Executor executor) {
        Objects.requireNonNull(fn);
        Objects.requireNonNull(executor);

        Promise<R> promise = Promise.promise();
        Consumer<AsyncResult<T>> consumer = ar -> {
            Asyncable<? extends R> apply = fn.apply(ar.result());
            apply.toFuture().addHandler(ar1 -> {
                if (ar1.succeeded()) {
                    promise.setSuccess(ar1.result());
                } else {
                    promise.setFailure(ar1.cause());
                }
            });
        };
        addHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    executor.execute(() -> {
                        try {
                            consumer.accept(ar);
                        } catch (Throwable e) {
                            promise.setFailure(e);
                        }
                    });
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            } else {
                promise.setFailure(ar.cause());
            }
        });
        return promise.toFuture();
    }

    /**
     * 当前future是异常结果时，将异常结果转换成正常结果
     *
     * @param fn 转换函数
     * @return 新的、可链式的Future
     */
    @Override
    default Future<T> otherwise(Function<? super Throwable, ? extends T> fn) {
        Objects.requireNonNull(fn);
        Promise<T> promise = Promise.promise();
        addHandler(ar -> {
            if (ar.succeeded()) {
                promise.setSuccess(ar.result());
            } else {
                try {
                    T apply = fn.apply(ar.cause());
                    promise.setSuccess(apply);
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            }
        });
        return promise.toFuture();
    }

}
