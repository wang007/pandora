package com.github.pandora.listenable.future;

import com.github.pandora.listenable.executor.ListenableExecutor;
import com.github.pandora.listenable.executor.ListenableExecutorService;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * map,flatMap,otherwise等等，已经在{@link SimpleListenableFutureTest}测过了。
 */
public class ListenableFutureTaskTest {

    private static final ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

    private static final ListenableExecutorService executor = ListenableExecutor.create(single);

    @Test
    public void isCancelled() {
        //cancel方法有覆盖
    }

    @Test
    public void isDone() {
        //cancel方法有覆盖
    }

    @Test
    public void cancel() throws InterruptedException {
        final ListenableExecutorService executor0 = ListenableExecutor.create(Executors.newCachedThreadPool());
        ListenableFuture<String> future = executor0.submit(() -> {
            Thread.sleep(10000L);
            return "cancel";
        });
        executor0.execute(() -> {
            future.cancel(true);
        });
        future.addHandler(ar -> {
            assertTrue(future.isCompleted());
            assertTrue(future.isDone());
            assertTrue(future.isCancelled());
            assertTrue(ar.cause() instanceof CancellationException);
        });
        Thread.sleep(1000);
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        final ListenableExecutorService executor0 = ListenableExecutor.create(Executors.newCachedThreadPool());
        ListenableFuture<String> future = executor0.submit(() -> {
            Thread.sleep(1000L);
            return "get";
        });
        String s = future.get();
        assertEquals("get", s);

        future.addHandler(ar -> {
            assertTrue(future.isCompleted());
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());
            assertEquals("get", ar.result());
        });
    }

    //timeout
    @Test
    public void get1() throws InterruptedException {

        final ListenableExecutorService executor0 = ListenableExecutor.create(Executors.newCachedThreadPool());
        ListenableFuture<String> future = executor0.submit(() -> {
            Thread.sleep(500);
            return "get";
        });
        try {
            future.get(200, TimeUnit.MILLISECONDS);
            throw new RuntimeException("rt");
        } catch (Exception e) {
            assertTrue(e instanceof TimeoutException);
        }
        future.addHandler(ar -> {
            assertTrue(future.isCompleted());
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());
            assertEquals("get", ar.result());
        });
        Thread.sleep(1000);
    }

    @Test
    public void addHandler() {
        //get cancel方法中均有覆盖
    }

    @Test
    public void handlers() throws InterruptedException {
        final ListenableExecutorService executor0 = ListenableExecutor.create(Executors.newCachedThreadPool());
        ListenableFuture<String> future = executor0.submit(() -> {
            Thread.sleep(500);
            return "get";
        });
        future.addHandler(ar -> {}).addHandler(ar -> {});
        assertEquals(future.handlers().size(), 2);

        Thread.sleep(1000);
    }

    @Test
    public void toCompletableResult() throws InterruptedException {
        final ListenableExecutorService executor0 = ListenableExecutor.create(Executors.newCachedThreadPool());
        ListenableFuture<String> future = executor0.submit(() -> {
            Thread.sleep(500);
            return "get";
        });
        future.toCompletableResult().addHandler(ar -> {
            System.out.println("result -> " + ar);
           assertEquals(ar.result(), "get");
        });

        Thread.sleep(1000);
    }
}