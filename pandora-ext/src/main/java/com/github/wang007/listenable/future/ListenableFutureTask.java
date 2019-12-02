package com.github.wang007.listenable.future;

import com.github.wang007.asyncResult.AsyncResult;
import com.github.wang007.asyncResult.AsyncStageResult;
import com.github.wang007.asyncResult.Future;
import com.github.wang007.asyncResult.Handler;
import com.github.wang007.listenable.executor.ListenableExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * created by wang007 on 2019/11/29
 */
public class ListenableFutureTask<V> extends FutureTask<V> implements ListenableRunFuture<V> {

    /**
     * 异步回调处理器
     * 绝大多数情况下，handler只有一个，所以这里就不用list来保存，当有多个的时候
     */
    private Object handlers;

    private final ListenableExecutor carrierExecutor;

    public ListenableFutureTask(Callable<V> callable, ListenableExecutor carrierExecutor) {
        super(callable);
        this.carrierExecutor = carrierExecutor;
    }

    public ListenableFutureTask(Runnable runnable, V result, ListenableExecutor carrierExecutor) {
        super(runnable, result);
        this.carrierExecutor = carrierExecutor;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        ifWarningForGet();
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ifWarningForGet();
        return super.get(timeout, unit);
    }

    @Override
    public ListenableExecutor carrierExecutor() {
        return carrierExecutor;
    }

    @Override
    protected synchronized void done() {
        Runnable run = () -> {
            List<Handler<AsyncResult<V>>> handlers = handlers();
            handlers.forEach(ls -> {
                try {
                    ls.handle(getAsAsyncResult());
                } catch (Throwable e) {
                    _logger.warn("execute onCompleted failed.", e);
                }
            });
        };
        if(isCancelled()) {  //唯一的入口来自cancel方法，
            carrierExecutor().execute(run);
        } else {
            run.run();
        }
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
    public ListenableFuture<V> addHandler(Handler<AsyncResult<V>> handler) {
        if (isDone()) {
            //回到carrierExecutor执行handler
            if(carrierExecutor().inCurrentExecutor()) {
                handler.handle(getAsAsyncResult());
            } else {
                carrierExecutor().execute(() -> handler.handle(getAsAsyncResult()));
            }
            return this;
        }

        synchronized (this) {
            if (handlers == null) handlers = handler;
            else if (handlers instanceof List) {
                List<Handler<?>> lfs = (List<Handler<?>>) handlers;
                lfs.add(handler);
            } else {
                List<Handler<?>> lfs = new ArrayList<>();
                lfs.add(handler);
                lfs.add(handler);
                this.handlers = lfs;
            }
        }
        return this;
    }



    @Override
    public List<Handler<AsyncResult<V>>> handlers() {
        Object handlers = this.handlers;
        if (handlers == null) return Collections.emptyList();
        else if (handlers instanceof List) {
            List<Handler<AsyncResult<V>>> lfs = (List<Handler<AsyncResult<V>>>) handlers;
            return Collections.unmodifiableList(lfs);
        }
        return Collections.singletonList((Handler<AsyncResult<V>>) handlers);
    }


    protected void ifWarningForGet() {
        if (!isDone() && !ListenableRunFuture._Disable_Warning_Get_OnBlocking) {
            _logger.warn("don't call #get or #get(timeout) directly, and instance of addListener or thenApply or...");
        }
    }


    @Override
    public boolean isCompleted() {
        //TODO
        return false;
    }

    @Override
    public Future<V> toFuture() {
        //TODO
        return null;
    }

    @Override
    public CompletionStage<V> toCompletionStage() {
        //TODO
        return null;
    }

    @Override
    public AsyncStageResult<V> toAsyncStageResult() {
        return null;
    }

}
