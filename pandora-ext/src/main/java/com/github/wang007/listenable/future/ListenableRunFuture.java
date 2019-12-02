package com.github.wang007.listenable.future;

import java.util.concurrent.RunnableFuture;

/**
 * 可{@link Runnable} 的 {@link ListenableFuture}
 * 作为可提交到{@link java.util.concurrent.Executor}的任务
 *
 *
 * created by wang007 on 2019/11/29
 */
public interface ListenableRunFuture<V> extends RunnableFuture<V>, ListenableFuture<V> {

    /**
     * 是否关闭
     */
    boolean _Disable_Warning_Get_OnBlocking = Boolean.getBoolean("disable.wang007.warning.get.onBlocking");
}
