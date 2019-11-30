package com.github.wang007.listenable.future;

import com.github.wang007.listenable.AsyncResult;

/**
 * listener on {@link ListenableFuture} completed
 *
 * see {@link AsyncResult}
 * see {@link ListenableFuture}
 *
 * created by wang007 on 2019/11/29
 */
@FunctionalInterface
public interface FutureListener<T> {

    /**
     *
     * @param result result
     */
    void onCompleted(AsyncResult<T> result);
}
