package com.github.wang007.expDsl;

import com.github.wang007.asyncResult.AsyncResult;
import com.github.wang007.asyncResult.AsyncResultImpl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * try-catch链式表达，{@link #doTry(Callable)}
 * <p>
 * 一旦实例化的Try，那么结果状态必须是明确的
 * <p>
 * idea from  Scala Try API
 * <p>
 * created by wang007 on 2019/12/4
 */
public interface Try<T> extends AsyncResult<T> {

    static <R> Try<R> succeededTry(R r) {
        return new _TryImpl<>(true, r);
    }

    static <R> Try<R> failedTry(Throwable err) {
        Objects.requireNonNull(err);
        return new _TryImpl<>(false, err);
    }

    static <R> Try<R> doTry(Callable<R> tryBlock) {
        Objects.requireNonNull(tryBlock);
        try {
            R call = tryBlock.call();
            return succeededTry(call);
        } catch (Throwable e) {
            return failedTry(e);
        }
    }

    /**
     * 获取结果。如果{@link #succeeded() == true}正常获取结果。
     * 如果{@link #failed() == true} throw {@link IllegalStateException}
     *
     * @return T
     */
    @Override
    T result();

    /**
     * {@link #succeeded()} 执行{@link Optional#ofNullable(Object)},可能存在null的情况
     * {@link #failed()} 执行{@link Optional#empty()}, 一定是null
     *
     * @return {@link Optional} 当且仅当 {@link #succeeded(),#result() != null}时，有值。
     */
    default Optional<T> toOptional() {
        return succeeded() ? Optional.ofNullable(result()) : Optional.empty();
    }


    /**
     * 获取异常结果。如果{@link #succeeded() == true} throw {@link IllegalStateException}
     * 如果{@link #failed() == true} 正常获取异常结果。
     *
     * @return err
     */
    @Override
    Throwable cause();

    default T getOrElse(T orElse) {
        return succeeded() ? result() : orElse;
    }

    /**
     * @param try0 必须是{@link #succeeded() == true}的Try实例, 否则throw {@link IllegalStateException}
     * @return
     */
    default T orElse(Try<T> try0) {
        return succeeded() ? result() : try0.result();
    }

    /**
     * alias {@link #result()}
     *
     * @return {@link #result()}
     */
    default T get() {
        return result();
    }


    default <U> Try<U> flatMap(Function<? super T, ? extends Try<U>> mapper) {
        if (succeeded()) {
            try {
                return mapper.apply(result());
            } catch (Throwable e) {
                return failedTry(e);
            }
        }
        return (Try<U>) this;
    }

    @Override
    default <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        if (succeeded()) {
            try {
                return succeededTry(mapper.apply(result()));
            } catch (Throwable e) {
                return failedTry(e);
            }
        }
        return (Try<U>) this;
    }

    @Override
    default <V> Try<V> map(V value) {
        return map(t -> value);
    }

    @Override
    default Try<T> otherwise(Function<? super Throwable, ? extends T> mapper) {
        if (failed()) {
            try {
                return succeededTry(mapper.apply(cause()));
            } catch (Throwable err) {
                return failedTry(err);
            }
        }
        return this;
    }

    @Override
    default Try<T> otherwise(T value) {
        return otherwise(err -> value);
    }

    class _TryImpl<T> extends AsyncResultImpl<T> implements Try<T> {

        protected _TryImpl(boolean succeeded, Object outcome) {
            super(succeeded, outcome);
        }

        @Override
        public T result() {
            if (succeeded()) return super.result();
            throw new IllegalStateException("failed Try");
        }

        @Override
        public Throwable cause() {
            if (failed()) return super.cause();
            throw new IllegalStateException("succeeded Try");
        }
    }
}


