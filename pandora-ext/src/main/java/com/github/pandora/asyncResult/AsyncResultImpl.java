package com.github.pandora.asyncResult;

/**
 * 该对象不存在中间状态。要么是成功，要么是失败
 * immutable object
 *
 * created by wang007 on 2019/11/29
 */
public class AsyncResultImpl<T> implements AsyncResult<T> {

    private final boolean succeeded;
    private final Object outcome;

    public AsyncResultImpl(boolean succeeded, Object outcome) {
        this.succeeded = succeeded;
        if(!succeeded) {
            if(outcome == null) outcome = new NullPointerException("asyncResult");
            else if(!(outcome instanceof Throwable)) throw new IllegalArgumentException("failed: must be throwable");
        }
        this.outcome = outcome;
    }

    @Override
    public T result() {
        if(succeeded()) return (T) outcome;
        return null;
    }

    @Override
    public Throwable cause() {
        if(failed()) return (Throwable) outcome;
        return null;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return !succeeded();
    }
}
