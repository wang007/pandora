package com.github.wang007.asyncResult;

import java.util.function.Function;

/**
 * copy from Vert.x
 * <p>
 * 代表异步结果。使用{@link #result(),#cause()}之前必须调用{@link #succeeded(),#failed()}判断结果状态
 * <p>
 * created by wang007 on 2019/11/29
 */
public interface AsyncResult<T> {

    static <T> AsyncResult<T> succeeded(T result) {
        return new AsyncResultImpl<>(true, result);
    }

    static <T> AsyncResult<T> failed(Throwable failed) {
        return new AsyncResultImpl<>(false, failed);
    }

    /**
     * The result of the operation. This will be null if the operation failed.
     *
     * @return the result or null if the operation failed.
     */
    T result();

    /**
     * A Throwable describing failure. This will be null if the operation succeeded.
     *
     * @return the cause or null if the operation succeeded.
     */
    Throwable cause();

    /**
     * Did it succeed?
     *
     * @return true if it succeded or false otherwise
     */
    boolean succeeded();

    /**
     * Did it fail?
     *
     * @return true if it failed or false otherwise
     */
    boolean failed();

    /**
     * Apply a {@code mapper} function on this async result.<p>
     * <p>
     * The {@code mapper} is called with the completed value and this mapper returns a value. This value will complete the result returned by this method call.<p>
     * <p>
     * When this async result is failed, the failure will be propagated to the returned async result and the {@code mapper} will not be called.
     *
     * @param mapper the mapper function
     * @return the mapped async result
     */
    default <U> AsyncResult<U> map(Function<? super T, ? extends U> mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        if (succeeded()) {
            try {
                U apply = mapper.apply(result());
                return succeeded(apply);
            } catch (Throwable e) {
                return failed(e);
            }
        }
        return (AsyncResult<U>) this;
    }

    /**
     * Map the result of this async result to a specific {@code value}.<p>
     * <p>
     * When this async result succeeds, this {@code value} will succeeed the async result returned by this method call.<p>
     * <p>
     * When this async result fails, the failure will be propagated to the returned async result.
     *
     * @param value the value that eventually completes the mapped async result
     * @return the mapped async result
     */
    default <V> AsyncResult<V> map(V value) {
        return map(t -> value);
    }

    /**
     * Apply a {@code mapper} function on this async result.<p>
     * <p>
     * The {@code mapper} is called with the failure and this mapper returns a value. This value will complete the result returned by this method call.<p>
     * <p>
     * When this async result is succeeded, the value will be propagated to the returned async result and the {@code mapper} will not be called.
     *
     * @param mapper the mapper function
     * @return the mapped async result
     */
    default AsyncResult<T> otherwise(Function<? super Throwable, ? extends T> mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        if (failed()) {
            try {
                T apply = mapper.apply(cause());
                return succeeded(apply);
            } catch (Throwable e) {
                return failed(e);
            }
        }
        return this;
    }

    /**
     * Map the failure of this async result to a specific {@code value}.<p>
     * <p>
     * When this async result fails, this {@code value} will succeeed the async result returned by this method call.<p>
     * <p>
     * When this async succeeds, the result will be propagated to the returned async result.
     *
     * @param value the value that eventually completes the mapped async result
     * @return the mapped async result
     */
    default AsyncResult<T> otherwise(T value) {
        return otherwise(err -> value);
    }

}
