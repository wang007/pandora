package com.github.wang007.listenable.future;

import com.github.wang007.asyncResult.Promise;
import java.util.concurrent.CompletionStage;

/**
 * copy from netty
 *
 * Special {@link ListenableFuture} which is writable.
 *
 * created by wang007 on 2019/11/30
 */
public interface ListenablePromise<V> extends ListenableFuture<V>, Promise<V> {

    @Override
    default ListenablePromise<V> setSuccess(V result) {
        Promise.super.setSuccess(result);
        return this;
    }

    @Override
    default ListenablePromise<V> setFailure(Throwable cause) {
        Promise.super.setFailure(cause);
        return this;
    }

    @Override
    default CompletionStage<V> toCompletionStage() {
        return null;
    }
}
