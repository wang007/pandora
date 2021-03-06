package com.github.pandora.asyncResult;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * 工具类
 *
 * 用于快创建异步化的对象
 *
 * 创建{@link Asyncable}对象，可使用{@link #succeededFuture(Object),#failedFuture(Throwable)}, {@link #succeededStage(Object),#failedResult(Throwable)}
 *
 *
 * created by wang007 on 2019/12/4
 */
public interface Async {

    /**
     * 创建未完成的Promise.
     *
     * @return {@link Promise}
     */
    static <T> Promise<T> promise() {
        return Promise.promise();
    }

    /**
     * 创建已完成正常结果的{@link Future,Asyncable}
     *
     * @param t 正常结果
     * @return {@link Future,Asyncable}
     */
    static <T> Future<T> succeededFuture(T t) {
        return succeededResult(t).toFuture();
    }

    /**
     * 创建已完成异常结果的{@link Future,Asyncable}
     * @param err 异常结果
     * @return {@link Future,Asyncable}
     */
    static <T> Future<T> failedFuture(Throwable err) {
        return Async.<T>failedResult(err).toFuture();
    }

    /**
     * 创建已完成正常结果的{@link CompletionStage}
     *
     * @param t 正常结果
     * @return {@link CompletionStage}
     */
    static <T> CompletionStage<T> succeededStage(T t) {
        return Async.succeededResult(t).toCompletionStage();
    }

    /**
     * 创建已完成异常结果的{@link CompletionStage}
     * @param err 异常结果
     * @return {@link CompletionStage}
     */
    static <T> CompletionStage<T> failedStage(Throwable err) {
        return Async.<T>failedResult(err).toCompletionStage();
    }

    /**
     * 创建已完成正常结果的{@link CompletableResult,Asyncable}
     *
     * @param t 正常结果
     * @return {@link CompletableResult,Asyncable}
     */
    static <T> CompletableResult<T> succeededResult(T t) {
        Promise<T> promise = Promise.promise();
        return promise.setSuccess(t).toCompletableResult();
    }

    /**
     * 创建已完成异常结果的{@link CompletableResult,Asyncable}
     * @param err 异常结果
     * @return {@link CompletableResult,Asyncable}
     */
    static <T> CompletableResult<T> failedResult(Throwable err) {
        Promise<T> promise = Promise.promise();
        return promise.setFailure(err).toCompletableResult();
    }

    /**
     * 包装{@link CompletionStage},转换成{@link CompletableResult,Asyncable}
     *
     * @param cs {@link CompletionStage}
     * @return {@link CompletableResult,Asyncable}
     */
    static <R> CompletableResult<R> wrap(CompletionStage<R> cs) {
        Objects.requireNonNull(cs);
        Promise<R> promise = Promise.promise();
        cs.whenComplete((r, err) -> {
            AsyncResult<R> ar;
            if (err != null) {
                ar = AsyncResult.failed(err);
            } else {
                ar = AsyncResult.succeeded(r);
            }
            promise.handle(ar);
        });
        return promise.toCompletableResult();
    }

    /**
     * 包装{@link Asyncable},转换成{@link CompletableResult}
     *
     * @param aa {@link Asyncable}
     * @return {@link CompletableResult}
     */
    static <R> CompletableResult<R> wrap(Asyncable<R> aa) {
        Objects.requireNonNull(aa);
        Promise<R> promise = Promise.promise();
        aa.toFuture().addHandler(promise);
        return promise.toCompletableResult();
    }

}
