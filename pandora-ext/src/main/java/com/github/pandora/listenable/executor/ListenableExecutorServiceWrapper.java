package com.github.pandora.listenable.executor;

import com.github.pandora.listenable.future.ListenableFuture;
import com.github.pandora.listenable.future.ListenableRunFuture;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * created by pandora on 2019/11/29
 */
public class ListenableExecutorServiceWrapper extends ListenableExecutorWrapper
        implements ListenableExecutorService {

    private final ExecutorService delegate;

    public ListenableExecutorServiceWrapper(ExecutorService delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected ExecutorService delegate() {
        return this.delegate;
    }

    @Override
    public void shutdown() {
        delegate().shutdown();

    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        ListenableRunFuture<T> runTask = newTaskFor(task);
        delegate().execute(runTask);
        return runTask;
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result) {
        ListenableRunFuture<T> runTask = newTaskFor(() -> {
            task.run();
            return result;
        });
        delegate().execute(runTask);
        return runTask;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate().invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().invokeAll(tasks,timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().invokeAny(tasks, timeout, unit);
    }
}
