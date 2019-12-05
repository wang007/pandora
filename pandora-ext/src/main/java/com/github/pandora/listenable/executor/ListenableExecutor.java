package com.github.pandora.listenable.executor;

import com.github.pandora.listenable.future.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 可监听执行结果的executor
 *
 * see {@link ListenableFuture}
 *
 * created by pandora on 2019/11/29
 */
public interface ListenableExecutor extends Executor {

    static ListenableExecutor create(Executor delegate) {
        return new ListenableExecutorWrapper(delegate);
    }

    static ListenableExecutorService create(ExecutorService delegate) {
        return new ListenableExecutorServiceWrapper(delegate);
    }

    /**
     * 提交任务，返回可监听结果的future
     * @param task 任务
     * @return ListenableFuture
     */
    ListenableFuture<Void> submit(Runnable task);

}
