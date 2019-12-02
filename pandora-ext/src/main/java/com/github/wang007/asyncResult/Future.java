package com.github.wang007.asyncResult;


import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 当前future所有操作，都不会产生阻塞。
 *
 * {@link java.util.concurrent.Future}是阻塞式api
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

    @Override
    default Future<T> toFuture() {
        return this;
    }

    /**
     * 将类型T转换成类型R.
     *
     * 操作符fn的异常统一抓起来，在最后时统一使用{@link #addHandler(Handler)}处理。异常可在操作符上流通。
     *
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenApply}.
     *
     * @param fn 转换函数
     * @param <R> 类型R
     * @return 新的、可链式的Future
     */
    @Override
    default <R> Future<R> map(Function<? super T, ? extends R> fn) {
        return null;
    }

    /**
     * 将包含类型T的future，转换成包含类型R的Future.
     *
     * 此操作符可平铺callback，解决callback hell
     *
     * 操作符fn的异常统一抓起来，在最后时统一使用{@link #addHandler(Handler)}处理。异常可在操作符上流通。
     *
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenCompose}.
     *
     * @param fn continuation
     * @param <R> 类型R
     * @return 新的、可链式的Future
     */
    default <R> Future<R> flatMap(Function<? super T, ? extends Future<? extends R>> fn) {
        return null;
    }

    /**
     * 此操作等价于{@link java.util.concurrent.CompletionStage#thenComposeAsync(Function, Executor)}.
     *
     * 此操作符可平铺callback，解决callback hell
     *
     * @see #flatMap(Function)
     * @param fn continuation
     * @param executor 将fn执行在声明的executor上。 executor == null，等价于{@link #flatMap(Function)}
     * @param <R> 类型R
     * @return 新的、可链式的Future
     */
    default <R> Future<R> flatMap(Function<? super T, ? extends Future<? extends R>> fn, Executor executor) {
        return null;
    }

    /**
     * 当前future是异常结果时，将异常结果转换成正常结果
     *
     * @param fn 转换函数
     * @return 新的、可链式的Future
     */
    @Override
    default Future<T> otherwise(Function<? super Throwable, ? extends T> fn) {
        return null;
    }

}
