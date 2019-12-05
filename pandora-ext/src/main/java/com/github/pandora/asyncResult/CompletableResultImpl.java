package com.github.pandora.asyncResult;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;



/**
 * {@link Promise,Future, AsyncStageResult}的标准实现。
 * <p>
 * 此api非open api,非常不建议直接依赖这个对象
 * <p>
 * fuck {@link CompletableFuture} api，由于里面实现，添加操作符的时候，如果这个{@link CompletableFuture}已处于一个
 * 完成时的状态时，当前添加操作符所在的线程会帮助处理之前添加在该{@link CompletableFuture}的操作符，那么会导致操作符的处
 * 理调用线程不确定。(不同版本的java8 实现出入还挺大。) 因此为了确保回调出发点，重写了{@link CompletionStage}，而非直接继承{@link CompletableFuture}
 * <p>
 * <p>
 * created by wang007 on 2019/12/2
 */
public class CompletableResultImpl<T> implements CompletableResult<T>, Promise<T> {

    private static final Logger logger = LoggerFactory.getLogger(CompletableResultImpl.class);

    private static final AtomicReferenceFieldUpdater<CompletableResultImpl, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(CompletableResultImpl.class, Object.class, "result");
    private static final Object Success = new Object();  //设置结果为null

    private volatile Object result;

    private Object handlers;

    private static class CauseHolder {
        public Throwable cause;
        public CauseHolder(Throwable cause) {
            this.cause = cause;
        }
        public static CauseHolder cause(Throwable cause) {
            Objects.requireNonNull(cause, "cause");
            return new CauseHolder(cause);
        }
    }

    @Override
    public Future<T> addHandler(Handler<AsyncResult<T>> handler) {
        if (isCompleted()) {
            handler.handle(this);
            return this;
        }
        synchronized (this) {
            if (handlers == null) handlers = handler;
            else if (handlers instanceof List) {
                List<Handler<?>> lfs = (List<Handler<?>>) handlers;
                lfs.add(handler);
            } else {
                List<Handler<?>> lfs = new ArrayList<>();
                lfs.add((Handler<?>) handlers);
                lfs.add(handler);
                this.handlers = lfs;
            }
        }
        return this;
    }

    public synchronized List<Handler<AsyncResult<T>>> handlers() {
        Object handlers = this.handlers;
        if (handlers == null) return Collections.emptyList();
        else if (handlers instanceof List) {
            List<Handler<AsyncResult<T>>> lfs = (List<Handler<AsyncResult<T>>>) handlers;
            return Collections.unmodifiableList(lfs);
        }
        return Collections.singletonList((Handler<AsyncResult<T>>) handlers);
    }

    /**
     * 执行通知handler
     */
    protected void notifyHandlers() {
        handlers().forEach(ls -> {
            try {
                ls.handle(this);
            } catch (Throwable e) {
                logger.warn("execute handler#handle failed.", e);
            }
        });
    }

    @Override
    public T result() {
        return result == Success ? null : (T) result;
    }

    @Override
    public Throwable cause() {
        if (result instanceof CauseHolder) return ((CauseHolder) result).cause;
        return null;
    }

    @Override
    public boolean succeeded() {
        return result != null && !(result instanceof CauseHolder);
    }

    @Override
    public boolean failed() {
        return result != null && result instanceof CauseHolder;
    }

    @Override
    public boolean trySuccess(T result) {
        Object r = result == null ? Success : result;
        if (RESULT_UPDATER.compareAndSet(this, null, r)) {
            notifyHandlers();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        Throwable err = cause == null ? new NullPointerException("cause is null") : cause;
        if (RESULT_UPDATER.compareAndSet(this, null, CauseHolder.cause(err))) {
            notifyHandlers();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCompleted() {
        return result != null;
    }
}
