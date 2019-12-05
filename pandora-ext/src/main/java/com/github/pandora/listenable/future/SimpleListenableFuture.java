package com.github.pandora.listenable.future;

import com.github.pandora.listenable.executor.ListenableExecutor;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 可设置结果的{@link ListenableFuture}
 *
 * created by pandora on 2019/11/30
 */
public class SimpleListenableFuture<V> extends AbstractListenableFuture<V> implements ListenablePromise<V> {

    private static final AtomicReferenceFieldUpdater<SimpleListenableFuture, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SimpleListenableFuture.class, Object.class, "result");
    private static final Object Success = new Object();  //设置结果为null
    private static final Object Canceled = new Object(); //取消future

    private volatile Object result;

    public SimpleListenableFuture(ListenableExecutor executor) {
        super(executor);
    }

    @Override
    public boolean trySuccess(V result) {
        if(RESULT_UPDATER.compareAndSet(this, null, result)) {
            synchronized (this) {
                notifyAll();
            }
            notifyHandlers();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
         if(RESULT_UPDATER.compareAndSet(this, null, new CauseHolder(cause))) {
             synchronized (this) {
                 notifyAll();
             }
             notifyHandlers();
             return true;
         }
         return false;
    }

    /**
     *
     * @param mayInterruptIfRunning ignore，忽略掉参数，该future不作为Runnable
     * @return
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if(RESULT_UPDATER.compareAndSet(this, null, Canceled)) {
            //callback在carrierExecutor上执行
            synchronized (this) {
                notifyAll();
            }
            carrierExecutor().execute(this::notifyHandlers);
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return result == Canceled;
    }


    @Override
    public boolean isDone() {
        return result != null || result == Success || result == Canceled;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        ifWarningForGet();
        try {
            await(0,  false);
        } catch (TimeoutException e) {
            //not timeout
        }
        return result0();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(timeout <= 0) throw new IllegalArgumentException("timeout be > 0");
        Objects.requireNonNull(unit, "unit");
        ifWarningForGet();
        await(unit.toNanos(timeout), true);
        return result0();
    }

    /**
     *
     * @param timeoutNs 纳秒 -1: 没有超时
     */
    private void await(long timeoutNs, boolean ifTimeout) throws InterruptedException, TimeoutException {
        if(isDone()) return;
        if(Thread.interrupted()) throw new InterruptedException();
        long startTime = 0;
        if(ifTimeout) startTime = System.nanoTime();
        synchronized (this) {
            while (!isDone()) {
                if(ifTimeout) {
                    wait(timeoutNs / 1000000, (int)(timeoutNs % 1000000));
                } else {
                    wait();
                }
                if(ifTimeout) {
                    long now = System.nanoTime();
                    timeoutNs = timeoutNs - (now - startTime);
                    startTime = now;
                    if(timeoutNs <= 0) throw new TimeoutException();
                }
            }
        }
    }

    private V result0() throws ExecutionException {
        if(result == Success) return null;
        else if(result == Canceled) throw new CancellationException();
        else if(result instanceof CauseHolder) throw new ExecutionException(((CauseHolder)result).cause);
        return (V) result;
    }



    private static class CauseHolder {
        public Throwable cause;
        public CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }

}
