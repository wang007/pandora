package com.github.wang007.listenable.future;

import com.github.wang007.listenable.AsyncResult;
import com.github.wang007.listenable.executor.ListenableExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 *
 * created by wang007 on 2019/11/30
 */
public abstract class AbstractListenableFuture<V> implements ListenableFuture<V> {

    /**
     * 监听器
     * 绝大多数情况下，监听器只有一个，所以这里就不用list来保存，当有多个的时候
     */
    private Object listeners;

    private final ListenableExecutor carrierExecutor;

    public AbstractListenableFuture(ListenableExecutor executor) {
        this.carrierExecutor = executor;
    }


    @Override
    public ListenableExecutor carrierExecutor() {
        return carrierExecutor;
    }

    /**
     * 如果是{@link ExecutionException}，将拆出原始异常
     *
     * @return 获取当前future的结果并包装成AsyncResult
     */
    protected AsyncResult<V> getAsAsyncResult() {
        AsyncResult<V> ar;
        try {
            V v = get();
            ar = AsyncResult.succeeded(v);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause(); //unwind
            ar = AsyncResult.failed(cause);
        } catch (Exception e) {
            ar = AsyncResult.failed(e);
        }
        return ar;
    }

    @Override
    public ListenableFuture<V> addListener(FutureListener<V> listener) {
        if (isDone()) {
            //回到carrierExecutor执行listener
            if(carrierExecutor().inCurrentExecutor()) {
                listener.onCompleted(getAsAsyncResult());
            } else {
                carrierExecutor().execute(() -> listener.onCompleted(getAsAsyncResult()));
            }
            return this;
        }

        synchronized (this) {
            if (listeners == null) listeners = listener;
            else if (listeners instanceof List) {
                List<FutureListener<?>> lfs = (List<FutureListener<?>>) listeners;
                lfs.add(listener);
            } else {
                List<FutureListener<?>> lfs = new ArrayList<>();
                lfs.add((FutureListener<?>) listeners);
                lfs.add(listener);
                this.listeners = lfs;
            }
        }
        return this;
    }

    @Override
    public List<FutureListener<V>> listeners() {
        Object listeners = this.listeners;
        if (listeners == null) return Collections.emptyList();
        else if (listeners instanceof List) {
            List<FutureListener<V>> lfs = (List<FutureListener<V>>) listeners;
            return Collections.unmodifiableList(lfs);
        }
        return Collections.singletonList((FutureListener<V>) listeners);
    }

    /**
     * 执行通知listeners
     */
    protected void notifyListeners() {
        synchronized (this) {
            listeners().forEach(ls -> {
                try {
                    ls.onCompleted(getAsAsyncResult());
                } catch (Throwable e) {
                    _logger.warn("execute onCompleted failed.", e);
                }
            });
        }
    }

    protected void ifWarningForGet() {
        if (!isDone() && !ListenableRunFuture.Disable_Warning_Get_OnBlocking) {
            _logger.warn("don't call #get or #get(timeout) directly, and instance of addListener or thenApply or...");
        }
    }

}
