package com.github.wang007.listenable.future;

/**
 * copy from netty
 *
 * Special {@link ListenableFuture} which is writable.
 *
 * created by wang007 on 2019/11/30
 */
public interface ListenablePromise<V> extends ListenableFuture<V> {

    /**
     * Marks this future as a success and notifies all
     * listeners.
     *
     * @return {@code true} if and only if successfully marked this future as
     *         a success. Otherwise {@code false} because this future is
     *         already marked as either a success or a failure.
     */
    boolean trySuccess(V result);

    /**
     * Marks this future as a failure and notifies all
     * listeners.
     *
     * @return {@code true} if and only if successfully marked this future as
     *         a failure. Otherwise {@code false} because this future is
     *         already marked as either a success or a failure.
     */
    boolean tryFailure(Throwable cause);


}
