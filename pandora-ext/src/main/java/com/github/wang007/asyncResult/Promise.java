package com.github.wang007.asyncResult;

/**
 *
 * 代表{@link Future}的可写一端。
 *
 * {@link #toFuture()}，为了让Promise与Future分离。
 * {@link Promise}只关心写结果，而{@link Future} 只关心对异常结果的处理。
 *
 * created by wang007 on 2019/12/1
 */
public interface Promise<T> extends Handler<AsyncResult<T>>, Asyncable<T> {

    /**
     * 创建一个未完成的异常结果
     * @param <R> 类型R
     * @return this
     */
    static <R> Promise<R> promise() {
        return new CompletableResultImpl<>();
    }

    /**
     * Marks this future as a success and notifies all
     * listeners.
     *
     * If it is success or failed already it will throw an {@link IllegalStateException}.
     *
     * copy from Netty
     */
    default Promise<T> setSuccess(T result) {
        if(trySuccess(result)) return this;
        throw new IllegalStateException("result was already set");
    }

    /**
     * Marks this future as a success and notifies all
     * listeners.
     *
     * @return {@code true} if and only if successfully marked this future as
     *         a success. Otherwise {@code false} because this future is
     *         already marked as either a success or a failure.
     *
     * copy from Netty
     */
    boolean trySuccess(T result);

    /**
     * Marks this future as a failure and notifies all
     * listeners.
     *
     * If it is success or failed already it will throw an {@link IllegalStateException}.
     *
     * copy from Netty
     */
    default Promise<T> setFailure(Throwable cause) {
        if(tryFailure(cause)) return this;
        throw new IllegalStateException("result was already set");
    }

    /**
     * Marks this future as a failure and notifies all
     * listeners.
     *
     * @return {@code true} if and only if successfully marked this future as
     *         a failure. Otherwise {@code false} because this future is
     *         already marked as either a success or a failure.
     * copy from Netty
     */
    boolean tryFailure(Throwable cause);

    /**
     * 动态分发Success or failure
     * 
     * If it is success or failed already it will throw an {@link IllegalStateException}.
     * 
     * @see #setSuccess(Object)
     * @see #setFailure(Throwable) 
     * 
     * @param asyncResult 异步结果
     */
    @Override
    default void handle(AsyncResult<T> asyncResult) {
        if(asyncResult.succeeded()) {
            setSuccess(asyncResult.result());
        } else {
            setFailure(asyncResult.cause());
        }
    }



}
