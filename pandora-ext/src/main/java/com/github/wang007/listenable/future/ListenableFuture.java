package com.github.wang007.listenable.future;

import com.github.wang007.asyncResult.AsyncResult;
import com.github.wang007.asyncResult.Futureable;
import com.github.wang007.asyncResult.Handler;
import com.github.wang007.listenable.executor.ListenableExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 可监听的future
 *
 * @see #addHandler(Handler) 添加当前future完成时的处理。
 * @see #addHandler(Handler, Handler)，跟{@link #addHandler(Handler)}一样.
 *
 * 可使用{@link #map(Function),#flatMap(Function, Executor), or...}平铺回调处理器。
 * 可链式使用，flatMap，map方法等，最后添加{@link #addHandler(Handler)} 等方法中
 * 统一处理异常（这个可以提供极大的便利不需要在每个操作符中try catch处理异常），或处理最后一个操作的结果。
 *
 * eg：
 * ListenableExecutor.create(Executor).submit(() -> {
 *     //do something
 * }).flatMap(v -> {
 *    //do something. run on other runnable
 * }).flatMap(v -> {
 *     //do something. run on special executor and
 * }, executor)
 * .map(v -> {
 *     //do something. run on the same runnable
 * }).addHandler(v -> {
 *     //map操作符的结果。
 * }, err -> {
 *     //统一处理所有的异常
 * });
 *
 *
 * created by wang007 on 2019/11/28
 */
public interface ListenableFuture<V> extends Future<V>, Futureable<V> {

    Logger _logger = LoggerFactory.getLogger(ListenableFuture.class);

    /**
     * @param executor carrierExecutor
     * @param <T>      T
     * @return {@link ListenablePromise}
     */
    static <T> ListenablePromise<T> ofPromise(ListenableExecutor executor) {
        return new SimpleListenableFuture<>(executor);
    }

    /**
     * @return 实际执行listener所在的线程池
     */
    ListenableExecutor carrierExecutor();

    /**
     * add handler
     * <p>
     * note: 回到一定会执行在carrierExecutor上
     *
     * @param handler handler
     * @return this
     */
    ListenableFuture<V> addHandler(Handler<AsyncResult<V>> handler);

    /**
     * add handler
     * <p>
     * note: 回到一定会执行在carrierExecutor上
     *
     * @param onSucceeded 成功时，回调
     * @param onFailed    失败时，回调
     * @return this
     */
    default ListenableFuture<V> addHandler(Handler<V> onSucceeded, Handler<Throwable> onFailed) {
        Objects.requireNonNull(onSucceeded, "onSucceeded");
        Objects.requireNonNull(onFailed, "onFailed");
        addHandler(ar -> {
            if (ar.succeeded()) onSucceeded.handle(ar.result());
            else onFailed.handle(ar.cause());
        });
        return this;
    }

    /**
     * add Succeeded handler，如果是异常的future，打印异常
     * <p>
     * note: 回到一定会执行在carrierExecutor上
     *
     * @param onSucceeded 成功时，回调
     * @return this
     */
    default ListenableFuture<V> addOnSucceeded(Handler<V> onSucceeded) {
        Objects.requireNonNull(onSucceeded, "onSucceeded");
        addHandler(onSucceeded, err -> {
            _logger.warn("failed", err);
        });
        return this;
    }

    /**
     * 返回在该Future上的异步回调处理器
     *
     * @return listeners
     */
    List<Handler<AsyncResult<V>>> handlers();

    /**
     *
     * @param fn  fn
     * @param <R> R
     * @return result of next continuation
     * @see #flatMap(Function)
     */
    default <R> ListenableFuture<R> flatMap(Function<? super V, ? extends R> fn) {
        return flatMap(fn, null);
    }

    /**
     * 将结果映射处理成另一种结果。
     * <p>
     * note: {@link #map(Function)}的区别：map直接获取结果，立即执行。
     *       {@link #otherwise(Function)} 的区别：otherwise处理异常结果并转换成正常的结果，立即执行。
     *       #flatMap(Function, Executor) 会再次提交到线程池上执行fn。
     * <p>
     * 本方法通过{@link #addHandler(Handler)} 完成的，不会产生{@link #get(long, TimeUnit),#get()}阻塞。
     *
     * 最后可别忘了最后使用{@link #addHandler(Handler),#addListener(Handler, Handler),#addOnSucceeded(Handler)}
     * 获取最后一个操作符的执行结果。
     *
     * @param fn       fn
     * @param <R>      R
     * @param executor fn执行所在的线程池，null: 继续执行在{@link #carrierExecutor()}线程，后续的相关操作都会到这里新的Executor执行。
     * @return result of next continuation
     */
    default <R> ListenableFuture<R> flatMap(Function<? super V, ? extends R> fn, Executor executor) {
        ListenablePromise<R> then;

        ListenableExecutor carrierOnRunNext = executor == null ?
                carrierExecutor() : ListenableExecutor.create(executor); //execute next continuation on carrier
        if (executor == null) {
            then = ofPromise(carrierExecutor());
        } else {
            then = new SimpleListenableFuture<R>(carrierExecutor()) {
                @Override
                public ListenableExecutor carrierExecutor() {
                    return carrierOnRunNext;
                }
            };
        }
        addHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    carrierOnRunNext.submit(() -> {
                        R apply = fn.apply(ar.result());  //执行过程有try catch了
                        then.trySuccess(apply); //设置已经正常结果了，listener中就不必处理succeeded的情况了
                    }).addHandler(ar1 -> {
                        if (ar1.succeeded()) {
                            //ignore
                        } else {
                            then.tryFailure(ar1.cause());
                        }
                    });
                } catch (Throwable e) { //may be reject?
                    then.tryFailure(e);
                }
            } else {
                then.tryFailure(ar.cause());
            }
        });
        return then;
    }

    /**
     * @param fn  fn
     * @param <R> R
     * @return result of next continuation
     * @see #flatMap(Function, Executor)
     */
    default <R> ListenableFuture<R> map(Function<? super V, ? extends R> fn) {
        ListenablePromise<R> then = ofPromise(carrierExecutor());
        addHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    then.trySuccess(fn.apply(ar.result()));
                } catch (Throwable e) {
                    then.tryFailure(e);
                }
            } else {
                then.tryFailure(ar.cause());
            }
        });
        return then;
    }

    /**
     * 将异常的结果转换成正常的结果，立即执行，不会提交Runnable到线程上
     *
     * @see #flatMap(Function, Executor)
     * @param fn fn
     * @return result of next continuation
     */
    default  ListenableFuture<V> otherwise(Function<? super Throwable, ? extends V> fn) {
        ListenablePromise<V> then = ofPromise(carrierExecutor());
        addHandler(ar -> {
            if (ar.succeeded()) {
                then.trySuccess(ar.result());
            } else {
                try {
                    then.trySuccess(fn.apply(ar.cause()));
                } catch (Throwable e) {
                    then.tryFailure(e);
                }
            }
        });
        return then;
    }

}
