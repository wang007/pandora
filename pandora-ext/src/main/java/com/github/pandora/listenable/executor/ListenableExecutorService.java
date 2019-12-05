package com.github.pandora.listenable.executor;

import com.github.pandora.listenable.future.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * 可监听的ExecutorService
 *
 * created by wang007 on 2019/11/29
 */
public interface ListenableExecutorService extends ExecutorService, ListenableExecutor {

    @Override
    <T> ListenableFuture<T> submit(Callable<T> task);

    @Override
    <T> ListenableFuture<T> submit(Runnable task, T result);

    @Override
    ListenableFuture<Void> submit(Runnable task);
}
