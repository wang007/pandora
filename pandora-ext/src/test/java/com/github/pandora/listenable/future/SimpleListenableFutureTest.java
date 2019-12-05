package com.github.pandora.listenable.future;

import com.github.pandora.asyncResult.AsyncResult;
import com.github.pandora.listenable.executor.ListenableExecutor;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

public class SimpleListenableFutureTest {

    private static final ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

    private static final ListenableExecutor executor = ListenableExecutor.create(single);

    @Test
    public void carrierExecutor() {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        ListenableExecutor executor = ListenableExecutor.create(single);

        ListenablePromise<Object> promise = ListenableFuture.ofPromise(executor);
        assertEquals(promise.carrierExecutor(), executor);
    }

    @Test
    public void getAsAsyncResult() {
        //ignore addHandler包含了该方法
    }

    @Test
    public void addHandler() {
        ListenablePromise<Object> promise = ListenableFuture.ofPromise(executor);
        promise.addHandler(ar -> {
            assertEquals("promise", ar.result());
        });
        promise.setSuccess("promise");

    }

    @Test
    public void handlers() {
        ListenablePromise<Object> promise = ListenableFuture.ofPromise(executor);
        promise.addHandler(ar -> {}).addHandler(ar -> {});
        assertEquals(2, promise.handlers().size());
    }

    @Test
    public void notifyHandlers() {
        //ignore addHandler包含此方法
    }

    @Test
    public void ifWarningForGet() {
        //ignore
    }

    @Test
    public void toCompletableResult() {
        ListenablePromise<Object> promise = ListenableFuture.ofPromise(executor);
        assertNotNull(promise.toCompletableResult());
    }

    @Test
    public void ofPromise() {
        ListenablePromise<Object> promise = ListenableFuture.ofPromise(executor);
        assertNotNull(promise);
    }

    @Test
    public void flatMap() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        promise.flatMap(str ->  {
            assertEquals(str, "promise");
            ListenablePromise<Integer> ofPromise = ListenableFuture.ofPromise(promise.carrierExecutor());
            return ofPromise.setSuccess(str.length());
        }).addHandler(ar -> {
            assertEquals(ar.result(), 7);
        });
        promise.setSuccess("promise");

        //
        ListenablePromise<String> promise1 = ListenableFuture.ofPromise(executor);
        promise1.flatMap(str -> {
            if(str.equalsIgnoreCase("promise")) {
                throw new RuntimeException("rt");
            }
            ListenablePromise<Integer> ofPromise = ListenableFuture.ofPromise(promise1.carrierExecutor());
            return ofPromise.setSuccess(str.length());
        }).addHandler(ar -> {
            assertEquals(ar.cause().getMessage(), "rt");
        });
        promise1.setSuccess("promise");

    }


    @Test
    public void map() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        promise.map(str ->  {
            assertEquals(str, "promise");
            return str.length();
        }).addHandler(ar -> {
            assertTrue(ar.result().equals(7));
        });
        promise.setSuccess("promise");

        //
        ListenablePromise<String> promise1 = ListenableFuture.ofPromise(executor);
        promise1.map(str ->  {
            if("promise".equalsIgnoreCase(str)) {
                throw new RuntimeException("rt");
            }
            return "unknown";
        }).addHandler(ar -> {
            assertEquals(ar.cause().getMessage(), "rt");
        });
        promise1.setSuccess("promise");
    }

    @Test
    public void otherwise() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        promise.otherwise(Throwable::getMessage)
        .addHandler(ar -> {
            assertEquals(ar.result(), "otherwise");
        });
        promise.setFailure(new Error("otherwise"));

        //
        ListenablePromise<String> promise1 = ListenableFuture.ofPromise(executor);
        promise1.otherwise(err -> {
            if(err.getMessage().equalsIgnoreCase("otherwise")) {
                throw new RuntimeException("rt");
            }
            return "unknown";
        }).addHandler(ar -> {
            assertEquals(ar.cause().getMessage(), "rt");
        });
        promise1.setFailure(new Error("otherwise"));

    }

    @Test
    public void isCompleted() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        assertFalse(promise.isCompleted());
        promise.setSuccess("s");
        assertTrue(promise.isCompleted());
    }

    @Test
    public void toFuture() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        assertNotNull(promise.toFuture());
    }

    @Test
    public void toCompletionStage() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        assertNotNull(promise.toCompletionStage());
    }

    @Test
    public void setSuccess() {
        //ignore  上面UT有覆盖
    }

    @Test
    public void setFailure() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        promise.setFailure(new Error("err"));
        promise.addHandler(ar -> {
            assertEquals(ar.cause().getMessage(), "err");
        });

    }

    @Test
    public void handle() {
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor);
        promise.handle(AsyncResult.succeeded("str"));
        promise.addHandler(ar -> {
            assertEquals(ar.result(), "str");
        });

        ListenablePromise<String> promise1 = ListenableFuture.ofPromise(executor);
        promise1.handle(AsyncResult.failed(new Error("error")));
        promise1.addHandler(ar -> {
            assertEquals(ar.cause().getMessage(), "error");
        });
    }

    @Test
    public void trySuccess() {
        //setSuccess 有覆盖
    }

    @Test
    public void tryFailure() {
        //setFailure 有覆盖
    }

    @Test
    public void cancel() {
        final ExecutorService single0 = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        final ListenableExecutor executor0 = ListenableExecutor.create(single0);

        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor0);
        promise.addHandler(ar -> {
            assertEquals(Thread.currentThread().getName(), "single");
        });

        single0.execute(() -> {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.cancel(true);
        });
        try {
            promise.get();
            throw new RuntimeException("unknown");
        } catch (Exception e) {
            assertTrue(e instanceof CancellationException);
        }
        assertTrue(promise.isCancelled());
        assertTrue(promise.isCompleted());
        assertTrue(promise.isDone());

    }

    @Test
    public void isCancelled() {
        //cancel 有覆盖
    }

    @Test
    public void isDone() {
        //cancel 有覆盖
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        final ExecutorService single0 = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        final ListenableExecutor executor0 = ListenableExecutor.create(single0);

        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor0);
        long start = System.currentTimeMillis();
        single0.execute(() -> {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.setSuccess("str");
        });
        assertEquals("str", promise.get());
        assertTrue((System.currentTimeMillis() - start) >= 500);
        assertTrue(promise.isDone());
        assertTrue(promise.isCompleted());
        assertFalse(promise.isCancelled());

    }

    //timeout
    @Test
    public void get1() {
        final ExecutorService single0 = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        final ListenableExecutor executor0 = ListenableExecutor.create(single0);
        ListenablePromise<String> promise = ListenableFuture.ofPromise(executor0);

        single0.execute(() -> {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.setSuccess("str");
        });
        try {
            promise.get(200, TimeUnit.MILLISECONDS);
            throw new RuntimeException("unknown");
        } catch (Exception e) {
            assertTrue(e instanceof TimeoutException);
        }

        ListenablePromise<String> promise1 = ListenableFuture.ofPromise(executor0);
        single0.execute(() -> {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise1.setSuccess("str");
        });
        try {
            String s = promise1.get(1000, TimeUnit.MILLISECONDS);
            assertEquals(s, "str");
        } catch (Exception e) {
            assertTrue(e instanceof TimeoutException);
        }
        assertTrue(promise1.isCompleted());
        assertTrue(promise1.isDone());
        assertFalse(promise1.isCancelled());
    }

}