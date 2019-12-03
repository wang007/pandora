package com.github.wang007.asyncResult;

import jdk.nashorn.internal.ir.annotations.Ignore;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
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
 * 尽管{@link CompletionStage}的api极其丑陋，但是没办法，它标准库api的实现。
 * <p>
 * created by wang007 on 2019/12/2
 */
public interface CompletableResult<T> extends Future<T>, CompletionStage<T>, Asyncable<T> {

    static <R> CompletableResult<R> wrap(CompletionStage<R> cs) {
        Objects.requireNonNull(cs);
        Promise<R> promise = Promise.promise();
        cs.handle((r, err) -> {
            AsyncResult<R> ar;
            if (err != null) {
                ar = AsyncResult.failed(err);
            } else {
                ar = AsyncResult.succeeded(r);
            }
            promise.handle(ar);
            return null;
        });
        return promise.toCompletableResult();
    }

    static <R> CompletableResult<R> wrap(Asyncable<R> aa) {
        Objects.requireNonNull(aa);
        Promise<R> promise = Promise.promise();
        aa.toFuture().addHandler(promise);
        return promise.toCompletableResult();
    }


    @Override
    default CompletableResult<T> toCompletableResult() {
        return this;
    }

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

    @Override
    default <U> CompletableResult<U> thenApply(Function<? super T, ? extends U> fn) {
        return map(fn);
    }

    @Override
    default <U> CompletableResult<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        Executor executor = ForkJoinPool.commonPool();
        return thenApplyAsync(fn, executor);
    }

    @Override
    default <U> CompletableResult<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        Objects.requireNonNull(executor, "executor");
        Future<U> map = Future.super.map(fn, executor);
        return map.toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenAccept(Consumer<? super T> action) {
        Function<? super T, Void> fun = t -> {
            action.accept(t);
            return null;
        };
        return map(fun).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenAcceptAsync(Consumer<? super T> action) {
        return thenAcceptAsync(action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        Objects.requireNonNull(executor, "executor");
        Function<? super T, Void> fun = t -> {
            action.accept(t);
            return null;
        };
        return map(fun, executor).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenRun(Runnable action) {
        Function<T, Void> fun = t -> {
            action.run();
            return null;
        };
        return map(fun).toCompletableResult();
    }

    @Override
    default CompletableResult<Void> thenRunAsync(Runnable action) {
        return thenRunAsync(action, ForkJoinPool.commonPool());
    }

    @Override
    default CompletableResult<Void> thenRunAsync(Runnable action, Executor executor) {
        Objects.requireNonNull(executor, "executor");
        Function<T, Void> fun = t -> {
            action.run();
            return null;
        };
        return map(fun, executor).toCompletableResult();
    }

    @Override
    default <U, V> CompletableResult<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(fn);
        Promise<V> promise = Promise.promise();


        CompletableResult<? extends U> other0 = wrap(other);
        other0.addHandler(ar -> {

        });
        return null;
    }

    @Override
    default <U, V> CompletableResult<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return null;
    }

    @Override
    default <U, V> CompletableResult<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return null;
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return null;
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return null;
    }

    @Override
    default <U> CompletableResult<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return null;
    }

    @Override
    default CompletableResult<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return null;
    }

    @Override
    default CompletableResult<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return null;
    }

    @Override
    default CompletableResult<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return null;
    }

    @Override
    default CompletableResult<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return null;
    }

    @Override
    default CompletableResult<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return null;
    }

    @Override
    default CompletableResult<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return null;
    }

    @Override
    default CompletableResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return null;
    }

    @Override
    default CompletableResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return null;
    }

    @Override
    default <U> CompletableResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return null;
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
