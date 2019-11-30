package com.github.wang007.listenable.executor;

import com.github.wang007.listenable.future.ListenableFuture;
import com.github.wang007.listenable.future.ListenableFutureTask;
import com.github.wang007.listenable.future.ListenableRunFuture;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * created by wang007 on 2019/11/29
 */
public class ListenableExecutorWrapper implements ListenableExecutor {

    public final Executor delegate;

    public ListenableExecutorWrapper(Executor delegate) {
        Objects.requireNonNull(delegate, "delegate");
        this.delegate = delegate;
    }

    protected ListenableRunFuture<Void> newTaskFor(Runnable task) {
        return new ListenableFutureTask<Void>(task, null, this);
    }

    protected <V> ListenableRunFuture<V> newTaskFor(Callable<V> task) {
        return new ListenableFutureTask<V>(task, this);
    }

    @Override
    public ListenableFuture<Void> submit(Runnable task) {
        ListenableRunFuture<Void> runTask = newTaskFor(task);
        execute(runTask);
        return runTask;
    }

    @Override
    public void execute(Runnable command) {
        delegate().execute(command);
    }

    protected Executor delegate() {
        return delegate;
    }
}
