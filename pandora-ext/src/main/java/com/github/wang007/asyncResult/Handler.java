package com.github.wang007.asyncResult;

/**
 * 统一的事件处理器。
 *
 * 通常作为异常结果的处理者。在异常结果完成时，通过该接口回调通知
 *
 * 例如: {@link com.github.wang007.listenable.future.ListenableFuture#addHandler(Handler)} 回调通知该监听的future结果已完成。
 *
 * created by wang007 on 2019/12/1
 */
@FunctionalInterface
public interface Handler<T> {

    void handle(T t);
}
