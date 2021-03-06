package com.github.pandora.listenable.future;

import com.github.pandora.asyncResult.Promise;
import java.util.concurrent.CompletionStage;

/**
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
        Promise<V> promise = Promise.promise();
        addHandler(promise);
        return promise.toCompletableResult();
    }
}
