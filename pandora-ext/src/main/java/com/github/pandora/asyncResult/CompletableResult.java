package com.github.pandora.asyncResult;

import com.github.pandora.listenable.executor.RunNowExecutor;
import com.github.pandora.utils.ObjectUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 异步结果的实现。与java8的CompletionStage异步api对接。该对象主要是连接java8的{@link CompletionStage}
 * <p>
 * 这里不继承标准库中的{@link java.util.concurrent.Future}，为了不提供阻塞的api，所有的异步结果必须使用回调处理。
 * 很遗憾的是{@link CompletionStage#toCompletableFuture()}接口api中提供了具体实现的api。fuck
 * <p>
 * 尽管{@link CompletionStage}的api极其丑陋，但是没办法，它标准库api的实现。方便在java开源库中做适配。
 *
 * 对{@link CompletionStage}做一个分类
 * <p>
 * 一：在异步结果正常完成时调用。区别在于入参和出参。 fuck api  相当于reactive#map
 * 1. {@link #thenApply(Function),#thenApplyAsync(Function),#thenApplyAsync(Function, Executor)}
 *
 * 2. {@link #thenAccept(Consumer),#thenAcceptAsync(Consumer),#thenAcceptAsync(Consumer, Executor)}
 *
 * 3. {@link #thenRun(Runnable),#thenRunAsync(Runnable),#thenAcceptAsync(Consumer, Executor)}
 * <p>
 * 二：当两个异步结果都正常完成时调用，区别在于入参和出参。fuck api  相当于reactive#zipWith
 * 1. {@link #thenCombine(CompletionStage, BiFunction),#thenCombineAsync(CompletionStage, BiFunction)}
 *    {@link #thenCombineAsync(CompletionStage, BiFunction, Executor)}
 *
 * 2. {@link #thenAcceptBoth(CompletionStage, BiConsumer),#thenAcceptBothAsync(CompletionStage, BiConsumer),
 *     {@link #thenAcceptBothAsync(CompletionStage, BiConsumer, Executor)}
 *
 * 3. {@link #runAfterBoth(CompletionStage, Runnable),#runAfterBothAsync(CompletionStage, Runnable)}
 *    {@link #runAfterBothAsync(CompletionStage, Runnable, Executor)}
 *
 * 三：两个异步结果任意其中之一正常完成时调用，区别在于入参和出参。 fuck api  相当于reactive#ambWith && map
 * 1.{@link #applyToEither(CompletionStage, Function),#applyToEitherAsync(CompletionStage, Function)}
 *   {@link #applyToEitherAsync(CompletionStage, Function, Executor)}
 *
 * 2. {@link #acceptEither(CompletionStage, Consumer),#acceptEitherAsync(CompletionStage, Consumer)}
 * {@link #acceptEitherAsync(CompletionStage, Consumer, Executor)}
 *
 * 3. {@link #runAfterEither(CompletionStage, Runnable),#runAfterEitherAsync(CompletionStage, Runnable)}
 *    {@link #runAfterEitherAsync(CompletionStage, Runnable, Executor)}
 *
 *
 * 四：当 前一个异步结果正常完成时产生一个新的不同类型的异步结果。  挺好。 相当于reactive#flatMap
 * 1. {@link #thenCompose(Function),#thenComposeAsync(Function, Executor),#thenComposeAsync(Function)}
 * <p>
 *
 * 五：当异步结果异常完成时调用  相当于reactive#onErrorReturn
 * 1. {@link #exceptionally(Function)}
 *
 * <p>
 * 六: 当异步结果完成(包括正常或异常)时调用。 相当于reactive#doOnSubscribe
 * 1.{@link #whenComplete(BiConsumer),#whenCompleteAsync(BiConsumer),#whenCompleteAsync(BiConsumer, Executor)}
 * 2. {@link #handle(BiFunction),#handleAsync(BiFunction),#handleAsync(BiFunction, Executor)}
 *
 * created by wang007 on 2019/12/2
 */
public interface CompletableResult<T> extends Future<T>, CompletionStage<T>, Asyncable<T> {


    @Override
    default CompletableResult<T> toCompletableResult() {
        return this;
    }

    //================================ Future ==================================

    @Override
    default <R> CompletableResult<R> map(Function<? super T, ? extends R> fn) {
        Future<R> fut = Future.super.map(fn);
        return fut.toCompletableResult();
    }

    @Override
    default <R> Future<R> map(Function<? super T, ? extends R> fn, Executor executor) {
        Future<R> fut = Future.super.map(fn, executor);
        return fut.toCompletableResult();
    }

    @Override
    default <R> CompletableResult<R> flatMap(Function<? super T, ? extends Asyncable<? extends R>> fn) {
        return Future.super.flatMap(fn).toCompletableResult();
    }

    @Override
    default <R> CompletableResult<R> flatMap(Function<? super T, ? extends Asyncable<? extends R>> fn, Executor executor) {
        return Future.super.flatMap(fn, executor).toCompletableResult();
    }

    @Override
    default CompletableResult<T> otherwise(Function<? super Throwable, ? extends T> fn) {
        return Future.super.otherwise(fn).toCompletableResult();
    }


    //================================= CompletionStage =========================

    @Override
    default <U> CompletableResult<U> thenApply(Function<? super T, ? extends U> fn) {
        return thenApplyAsync(fn, RunNowExecutor.Executor);
    }

    @Override
    default <U> CompletableResult<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return thenApplyAsync(fn, ForkJoinPool.commonPool());
    }

    @Override
    default <U> CompletableResult<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        Future<U> map = map(fn, executor);
        return map.toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenAccept(Consumer<? super T> action) {
        return thenAcceptAsync(action, RunNowExecutor.Executor).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenAcceptAsync(Consumer<? super T> action) {
        return thenAcceptAsync(action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        Objects.requireNonNull(action);
        Function<? super T, Void> fun = t -> {
            action.accept(t);
            return null;
        };
        return map(fun, executor).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenRun(Runnable action) {
        return thenRunAsync(action, RunNowExecutor.Executor).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenRunAsync(Runnable action) {
        return thenRunAsync(action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> thenRunAsync(Runnable action, Executor executor) {
        Objects.requireNonNull(action);
        Function<T, Void> fun = t -> {
            action.run();
            return null;
        };
        return map(fun, executor).toCompletableResult();
    }

    @Override
    default <U, V> CompletableResult<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return thenCombineAsync(other, fn, RunNowExecutor.Executor);
    }

    @Override
    default <U, V> CompletableResult<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return thenCombineAsync(other, fn, ForkJoinPool.commonPool());
    }

    @SuppressWarnings("unchecked")
    @Override
    default <U, V> CompletableResult<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        ObjectUtils.requireNonNull(other, fn, executor);

        Promise<V> promise = Promise.promise();
        CompletableResult<? extends U> other0 = Async.wrap(other);
        AtomicInteger successCount = new AtomicInteger();
        AtomicBoolean ifExec = new AtomicBoolean();
        AtomicReference<Object> ref0 = new AtomicReference<>();
        AtomicReference<Object> ref1 = new AtomicReference<>();

        BiConsumer<T, U> bc = (t, u) -> {
            try {
                executor.execute(() -> {
                    try {
                        V apply = fn.apply(t, u);
                        promise.setSuccess(apply);
                    } catch (Throwable e1) {
                        promise.setFailure(e1);
                    }
                });
            } catch (Throwable e) {
                promise.setFailure(cause());
            }
        };

        other0.addHandler(ar -> {
            if (ar.succeeded()) {
                ref0.set(ar.result());
                if (successCount.incrementAndGet() == 2 && ifExec.compareAndSet(false, true)) {
                    T t = (T) ref1.get();
                    U u = ar.result();
                    bc.accept(t, u);
                }
                return;
            }
            if (ifExec.compareAndSet(false, true)) promise.setFailure(ar.cause());
        });
        addHandler(ar -> {
            if (ar.succeeded()) {
                ref1.set(ar.result());
                if (successCount.incrementAndGet() == 2 && ifExec.compareAndSet(false, true)) {
                    T t = ar.result();
                    U u = (U) ref0.get();
                    bc.accept(t, u);
                }
                return;
            }
            if (ifExec.compareAndSet(false, true)) promise.setFailure(ar.cause());
        });
        return promise.toCompletableResult();
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return thenAcceptBothAsync(other, action, RunNowExecutor.Executor);
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return thenAcceptBothAsync(other, action, ForkJoinPool.commonPool());
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        Objects.requireNonNull(action);
        BiFunction<T, U, Void> bf = (t, u) -> {
            action.accept(t, u);
            return null;
        };
        return thenCombineAsync(other, bf, executor).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return runAfterBothAsync(other, action, RunNowExecutor.Executor);
    }

    @Override
    default CompletableResult<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return runAfterBothAsync(other, action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        Objects.requireNonNull(action);
        BiFunction<T, Object, Void> bf = (t, u) -> {
            action.run();
            return null;
        };
        return thenCombineAsync(other, bf, executor);
    }

    @Override
    default <U> CompletableResult<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return applyToEitherAsync(other, fn, RunNowExecutor.Executor);
    }

    @Override
    default <U> CompletableResult<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return applyToEitherAsync(other, fn , ForkJoinPool.commonPool());
    }

    @Override
    default <U> CompletableResult<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        ObjectUtils.requireNonNull(other, fn, executor);

        Promise<U> promise = Promise.promise();
        AtomicInteger failedCount = new AtomicInteger();
        AtomicBoolean ifExec = new AtomicBoolean();

        CompletableResult<? extends T> other0 = Async.wrap(other);

        Handler<AsyncResult<? extends T>> handler = ar -> {
            if (ar.succeeded()) {
                if (ifExec.compareAndSet(false, true)) {
                    try {
                        executor.execute(() -> {
                            try {
                                U apply = fn.apply(ar.result());
                                promise.setSuccess(apply);
                            } catch (Throwable e1) {
                                promise.setFailure(e1);
                            }
                        });
                    } catch (Throwable e) {
                        promise.setFailure(e);
                    }
                }
            } else {
                if (failedCount.incrementAndGet() == 2 && ifExec.compareAndSet(false, true)) {
                    promise.setFailure(ar.cause());
                }
            }
        };

        other0.addHandler(handler::handle);
        addHandler(handler::handle);

        return promise.toCompletableResult();
    }

    @Override
    default CompletableResult<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return acceptEitherAsync(other, action, RunNowExecutor.Executor);
    }

    @Override
    default CompletableResult<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return acceptEitherAsync(other, action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        Objects.requireNonNull(action);
        Function<T, Void> fn = t -> {
            action.accept(t);
            return null;
        };
        return applyToEitherAsync(other, fn, executor);
    }

    @Override
    default CompletableResult<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return runAfterEitherAsync(other, action, RunNowExecutor.Executor);
    }

    @Override
    default CompletableResult<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return runAfterEitherAsync(other, action, ForkJoinPool.commonPool());
    }

    @SuppressWarnings("unchecked")
    @Override
    default CompletableResult<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        Objects.requireNonNull(action);
        Function<T, Void> fn = t -> {
            action.run();
            return null;
        };
        CompletionStage<T> other0 = (CompletionStage<T>) other;
        return applyToEitherAsync(other0, fn, executor);
    }

    @Override
    default <U> CompletableResult<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenComposeAsync(fn, RunNowExecutor.Executor);
    }

    @Override
    default <U> CompletableResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenComposeAsync(fn, ForkJoinPool.commonPool());
    }

    @Override
    default <U> CompletableResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        ObjectUtils.requireNonNull(fn, executor);

        Promise<U> promise = Promise.promise();
        addHandler(ar -> {
            if(succeeded()) {
                try {
                    executor.execute(() -> {
                        try {
                            CompletionStage<U> apply = fn.apply(ar.result());
                            apply.whenComplete((u, err) -> {
                                if(err != null) {
                                    promise.setFailure(err);
                                } else {
                                    promise.setSuccess(u);
                                }
                            });
                        } catch (Throwable e1) {
                            promise.setFailure(e1);
                        }
                    });
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            } else {
                promise.setFailure(ar.cause());
            }
        });

        return promise.toCompletableResult();
    }

    @Override
    default CompletableResult<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return otherwise(fn).toCompletableResult();
    }

    @Override
    default CompletableResult<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return whenCompleteAsync(action, RunNowExecutor.Executor);
    }

    @Override
    default CompletableResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return whenCompleteAsync(action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        Objects.requireNonNull(action);
        BiFunction<T, Throwable, T> bf = (t, err) -> {
            action.accept(t, err);
            return t;
        };
        return handleAsync(bf, executor);
    }

    @Override
    default <U> CompletableResult<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handleAsync(fn, RunNowExecutor.Executor);
    }

    @Override
    default <U> CompletableResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handleAsync(fn, ForkJoinPool.commonPool());
    }

    @Override
    default <U> CompletableResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        ObjectUtils.requireNonNull(fn, executor);

        Promise<U> promise = Promise.promise();
        addHandler(ar -> {
            try {
                executor.execute(() -> {
                    try {
                        U apply = fn.apply(ar.result(), ar.cause());
                        promise.setSuccess(apply);
                    } catch (Throwable e1) {
                        promise.setFailure(e1);
                    }
                });
            } catch (Throwable e) {
                promise.setFailure(e);
            }

        });
        return promise.toCompletableResult();
    }

    @Override
    default CompletableFuture<T> toCompletableFuture() {
        CompletableFuture<T> fut = new CompletableFuture<>();
        addHandler(ar -> {
            if (ar.succeeded()) {
                if (!fut.complete(ar.result()))
                    throw new IllegalStateException("call CompletableFuture#complete failed");
                return;
            }
            if (fut.completeExceptionally(ar.cause()))
                throw new IllegalStateException("call CompletableFuture#completeExceptionally failed");
        });
        return fut;
    }

}
