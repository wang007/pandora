package com.github.wang007.listenable;

/**
 * 该对象不存在中间状态。要么是成功，要么是失败
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
        if(!succeeded) throw new IllegalStateException("failed status: not result.");
        return (T) outcome;
    }

    @Override
    public Throwable cause() {
        return (Throwable) outcome;
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
