package com.github.pandora.asyncResult;

import org.junit.Test;
import org.omg.PortableServer.THREAD_POLICY_ID;

import javax.swing.plaf.synth.SynthRadioButtonMenuItemUI;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class CompletableResultTest {

    @Test
    public void map() throws InterruptedException {

        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread("single"));
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .map(str -> {
                    assertEquals(str, "map");
                    int len = str.length();
                    return len;
                })
                .map(len -> {
                    assertEquals(len, new Integer(3));
                    return len + "" + len;
                }, single)
                .map(str -> {
                    if (str.equalsIgnoreCase("33")) {
                        throw new RuntimeException("rt");
                    }
                    return str;
                })
                .addHandler(ar -> {
                    assertTrue(ar.failed());
                    assertFalse(ar.succeeded());
                    assertNull(ar.result());
                    assertNull(ar.cause());
                    assertEquals(Thread.currentThread().getName(), "single");
                });

        new Thread(() -> promise.setSuccess("map"), "TTT").start();

        Thread.sleep(2000L);
    }

    //map(fn, executor);
    @Test
    public void map1() {
        //map方法覆盖
    }

    @Test
    public void flatMap() throws InterruptedException {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread("single"));
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .flatMap(str -> {
                    assertEquals(str, "flatMap");
                    return Async.succeededResult(str.length());
                }).flatMap(len -> {
            if (len == 7) {
                Promise<String> promise1 = Async.promise();
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        promise1.setSuccess(len + "" + len);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, "t").start();
                return promise1;
            } else {
                throw new RuntimeException("unknown....");
            }
        }).flatMap(str -> {
            assertEquals(Thread.currentThread().getName(), "t");
            if (str.equals("77")) throw new RuntimeException("rt");
            else throw new RuntimeException("unknown");
        }, single)
                .addHandler(ar -> {
                    assertEquals(Thread.currentThread().getName(), "single");
                    assertTrue(ar.failed());
                    assertFalse(ar.succeeded());
                });

        new Thread(() -> promise.setSuccess("flatMap"), "TTT").start();
        Thread.sleep(2000L);
    }

    //flatMap(fn, executor);
    @Test
    public void flatMap1() {
        //flatMap方法覆盖
    }

    @Test
    public void otherwise() {
        Promise<String> promise = Async.promise();

        promise.toCompletableResult()
                .map(str -> {
                    if (str.equals("otherwise")) {
                        throw new RuntimeException("rt");
                    }
                    throw new RuntimeException("unknown");
                })
                .otherwise(err -> {
                    if (err.getMessage().equals("rt")) {
                        return "rt";
                    }
                    throw new RuntimeException(err);
                })
                .addHandler(ar -> {
                    assertEquals(ar.result(), "rt");
                    assertTrue(ar.succeeded());
                });
    }

    @Test
    public void succeeded() {

        Promise<String> promise = Async.promise();
        assertFalse(promise.toCompletableResult().succeeded());
        assertFalse(promise.isCompleted());
        promise.setSuccess("");
        assertTrue(promise.toCompletableResult().succeeded());
        assertTrue(promise.isCompleted());
    }

    @Test
    public void failed() {
        Promise<String> promise = Async.promise();
        assertFalse(promise.toCompletableResult().failed());
        assertFalse(promise.isCompleted());
        promise.setFailure(new Error());
        assertTrue(promise.toCompletableResult().failed());
        assertTrue(promise.isCompleted());
    }


    @Test
    public void isCompleted() {
        //succeeded方法， failed方法均有覆盖
    }

    @Test
    public void toFuture() {
        Promise<String> promise = Async.promise();
        promise.toFuture().addHandler(ar -> {
            assertEquals("future", ar.result());
        });
        promise.setSuccess("future");
    }

    @Test
    public void toCompletionStage() {
        Promise<String> promise = Async.promise();
        promise.toCompletionStage()
                .whenComplete((str, err) -> {
                    assertEquals(str, "sss");
                    assertNull(err);
                });
        promise.setSuccess("sss");
    }

    @Test
    public void toCompletableResult() {
        //上面方法均有覆盖
    }


    @Test
    public void thenApply() {
        String mainName = Thread.currentThread().getName();

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenApply(String::length)
                .addHandler(ar -> {
                    assertEquals(Thread.currentThread().getName(), mainName);
                    assertEquals(ar.result(), new Integer(4));
                    assertTrue(ar.succeeded());
                });
        promise.setSuccess("then");

        Promise<String> promise2 = Async.promise();
        promise2.toCompletableResult()
                .thenApply(String::length)
                .addHandler(ar -> {
                    assertEquals(ar.cause().getMessage(), "error");
                    assertTrue(ar.failed());
                });
        promise2.setFailure(new Error("error"));

    }

    @Test
    public void thenApplyAsync() {
        String mainName = Thread.currentThread().getName();

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenApplyAsync(String::length)
                .addHandler(ar -> {
                    assertNotEquals(Thread.currentThread().getName(), mainName);
                    assertEquals(ar.result(), new Integer(4));
                    assertTrue(ar.succeeded());
                });
        promise.setSuccess("then");

        Promise<String> promise2 = Async.promise();
        promise2.toCompletableResult()
                .thenApplyAsync(String::length)
                .addHandler(ar -> {
                    assertEquals(ar.cause().getMessage(), "error");
                    assertTrue(ar.failed());
                });
        promise2.setFailure(new Error("error"));

    }

    @Test
    public void thenApplyAsync1() {

        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenApplyAsync(String::length, single)
                .addHandler(ar -> {
                    assertEquals(Thread.currentThread().getName(), "single");
                    assertEquals(ar.result(), new Integer(4));
                    assertTrue(ar.succeeded());
                });
        promise.setSuccess("then");
    }

    //thenAccept使用thenApplyAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void thenAccept() {
        String mainName = Thread.currentThread().getName();

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenAccept(str -> {
                    assertEquals(str, "then");
                    assertEquals(Thread.currentThread().getName(), mainName);
                })
                .addHandler(ar -> {
                    assertTrue(ar.succeeded());
                });
        promise.setSuccess("then");
    }

    @Test
    public void thenAcceptAsync() {
        //NOOP
    }

    @Test
    public void thenAcceptAsync1() {
        //NOOP
    }

    //thenRun使用thenApplyAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void thenRun() {
        String mainName = Thread.currentThread().getName();

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenRun(() -> {
                    System.out.println("run");
                    assertEquals(Thread.currentThread().getName(), mainName);
                })
                .addHandler(ar -> {
                    assertTrue(ar.succeeded());
                });
        promise.setSuccess("then");
    }

    @Test
    public void thenRunAsync() {
        //NOOP
    }

    @Test
    public void thenRunAsync1() {
        //NOOP
    }

    @Test
    public void thenCombine() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<Integer> promise2 = Async.promise();
        promise.toCompletableResult()
                .thenCombine(promise2.toCompletableResult(), (str, num) -> {
                    System.out.println("thenCombine thread -> " + Thread.currentThread().getName());
                    assertTrue(Arrays.asList(mainName, "single")
                            .contains(Thread.currentThread().getName()));
                    return str + num;
                })
                .addHandler(ar -> {
                    assertTrue(Arrays.asList(mainName, "single")
                            .contains(Thread.currentThread().getName()));
                    assertTrue(ar.succeeded());
                    assertTrue(ar.result().startsWith("then"));
                });
        promise.setSuccess("then");
        single.execute(() -> promise2.setSuccess(2333));

        Thread.sleep(1000);
    }

    @Test
    public void thenCombineAsync() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<Integer> promise2 = Async.promise();
        promise.toCompletableResult()
                .thenCombineAsync(promise2.toCompletableResult(), (str, num) -> {
                    System.out.println("thenCombineAsync thread -> " + Thread.currentThread().getName());
                    assertFalse(Arrays.asList(mainName, "single")
                            .contains(Thread.currentThread().getName()));
                    return str + num;
                })
                .addHandler(ar -> {
                    assertTrue(Arrays.asList(mainName, "single")
                            .contains(Thread.currentThread().getName()));
                    assertTrue(ar.failed());
                    assertEquals("error", ar.cause());
                });

        single.execute(() -> promise2.setSuccess(2333));
        promise.setFailure(new Error("error"));

        Thread.sleep(1000);
    }

    @Test
    public void thenCombineAsync1() throws InterruptedException {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<Integer> promise2 = Async.promise();
        promise.toCompletableResult()
                .thenCombineAsync(promise2.toCompletableResult(), (str, num) -> {
                    System.out.println("thenCombineAsync1 thread -> " + Thread.currentThread().getName());
                    assertEquals("single", Thread.currentThread().getName());
                    return str + num;
                }, single);
        promise.setSuccess("then");
        new Thread(() -> promise2.setSuccess(2333)).start();

        Thread.sleep(1000);
    }

    //thenAcceptBoth使用thenCombineAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void thenAcceptBoth() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<Integer> promise2 = Async.promise();
        promise.toCompletableResult()
                .thenAcceptBoth(promise2.toCompletableResult(), (str, num) -> {
                    System.out.println("thenAcceptBoth thread -> " + Thread.currentThread().getName());
                    assertTrue(Arrays.asList(mainName, "single")
                            .contains(Thread.currentThread().getName()));
                });

        promise.setSuccess("then");
        single.execute(() -> promise2.setSuccess(2333));
        Thread.sleep(1000);
    }

    @Test
    public void thenAcceptBothAsync() {
        //NOOP
    }

    @Test
    public void thenAcceptBothAsync1() {
        //NOOP
    }

    //runAfterBoth使用thenCombineAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void runAfterBoth() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<Integer> promise2 = Async.promise();
        promise.toCompletableResult().runAfterBoth(promise2.toCompletionStage(), () -> {
            System.out.println("runAfterBoth thread -> " + Thread.currentThread().getName());
            assertTrue(Arrays.asList(mainName, "single")
                    .contains(Thread.currentThread().getName()));
        });

        promise.setSuccess("then");
        single.execute(() -> promise2.setSuccess(2333));
        Thread.sleep(1000);
    }

    @Test
    public void runAfterBothAsync() {
        //NOOP
    }

    @Test
    public void runAfterBothAsync1() {
        //NOOP
    }

    @Test
    public void applyToEither() throws InterruptedException {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<String> promise2 = Async.promise();
        promise.toCompletableResult()
                .applyToEither(promise2.toCompletionStage(), str -> {
                    System.out.println("applyToEither thread -> " + Thread.currentThread().getName());
                    assertEquals("single", Thread.currentThread().getName());
                    return str.length();
                })
                .addHandler(ar -> {
                    assertEquals(5, (int) ar.result());
                });
        promise.setFailure(new Error("error"));
        single.execute(() -> promise2.setSuccess("apply"));

        Thread.sleep(1000);
    }

    @Test
    public void applyToEitherAsync() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<String> promise2 = Async.promise();
        promise.toCompletableResult()
                .applyToEitherAsync(promise2.toCompletionStage(), str -> {
                    System.out.println("applyToEitherAsync thread -> " + Thread.currentThread().getName());
                    assertFalse(Arrays.asList(mainName, "single").contains(Thread.currentThread().getName()));
                    return str.length();
                })
                .addHandler(ar -> {
                    assertEquals(5, (int) ar.result());
                });
        promise.setFailure(new Error("error"));
        single.execute(() -> promise2.setSuccess("apply"));

        Thread.sleep(1000);
    }

    @Test
    public void applyToEitherAsync1() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<String> promise2 = Async.promise();
        promise.toCompletableResult()
                .applyToEitherAsync(promise2.toCompletionStage(), str -> {
                    System.out.println("applyToEitherAsync1 thread -> " + Thread.currentThread().getName());
                    assertEquals("single", Thread.currentThread().getName());
                    return str.length();
                }, single)
                .addHandler(ar -> {
                    assertEquals(5, (int) ar.result());
                });
        promise.setFailure(new Error("error"));
        new Thread(() -> promise2.setSuccess("apply")).start();

        Thread.sleep(1000);
    }

    //acceptEither使用applyToEitherAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void acceptEither() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<String> promise2 = Async.promise();
        promise.toCompletableResult()
                .acceptEither(promise2.toCompletionStage(), str -> {
                    System.out.println("acceptEither thread -> " + Thread.currentThread().getName());
                    assertTrue(Arrays.asList("single", mainName).contains(Thread.currentThread().getName()));
                });

        promise.setFailure(new Error("error"));
        single.execute(() -> promise2.setSuccess("apply"));

        Thread.sleep(1000);
    }

    @Test
    public void acceptEitherAsync() {
        //NOOP
    }

    @Test
    public void acceptEitherAsync1() {
        //NOOP
    }

    //runAfterEither使用applyToEitherAsync实现的，所以只需要测一个即可覆盖完成
    @Test
    public void runAfterEither() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        Promise<String> promise2 = Async.promise();
        promise.toCompletableResult()
                .runAfterEither(promise2.toCompletionStage(), () -> {
                    System.out.println("acceptEither thread -> " + Thread.currentThread().getName());
                    assertTrue(Arrays.asList("single", mainName).contains(Thread.currentThread().getName()));
                });

        promise.setFailure(new Error("error"));
        single.execute(() -> promise2.setSuccess("apply"));

        Thread.sleep(1000);
    }

    @Test
    public void runAfterEitherAsync() {
        //NOOP
    }

    @Test
    public void runAfterEitherAsync1() {
        //NOOP
    }

    @Test
    public void thenCompose() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenCompose(str -> {
                    assertEquals(str, "then");
                    Promise<Integer> objectPromise = Async.promise();
                    Executors.newSingleThreadExecutor().submit(() -> {
                       Thread.sleep(200);
                        objectPromise.setSuccess(str.length());
                       return null;
                    });
                    return objectPromise.toCompletableResult();
                })
                .addHandler(ar -> {
                    assertEquals(ar.result(), new Integer(4));
                    System.out.println("thenCompose handler thread -> " + Thread.currentThread().getName());
                    assertFalse(Arrays.asList(mainName, "single").contains(Thread.currentThread().getName()));
                });

        single.execute(() -> promise.setSuccess("then"));

        Thread.sleep(1000);
    }

    @Test
    public void thenComposeAsync() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenComposeAsync(str -> {
                    System.out.println("thenComposeAsync thread -> " + Thread.currentThread().getName());
                    assertEquals(str, "then");
                    Promise<Integer> objectPromise = Async.promise();
                    Executors.newSingleThreadExecutor().submit(() -> {
                        Thread.sleep(200);
                        objectPromise.setSuccess(str.length());
                        return null;
                    });
                    return objectPromise.toCompletableResult();
                })
                .addHandler(ar -> {
                    assertEquals(ar.result(), new Integer(4));
                    System.out.println("thenComposeAsync handler thread -> " + Thread.currentThread().getName());
                    assertFalse(Arrays.asList(mainName, "single").contains(Thread.currentThread().getName()));
                });
        promise.setSuccess("then");

        Thread.sleep(1000);
    }

    @Test
    public void thenComposeAsync1() throws InterruptedException {
        String mainName = Thread.currentThread().getName();
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .thenComposeAsync(str -> {
                    assertEquals("single", Thread.currentThread().getName());
                    assertEquals(str, "then");
                    Promise<Integer> objectPromise = Async.promise();
                    Executors.newSingleThreadExecutor().submit(() -> {
                        Thread.sleep(200);
                        objectPromise.setSuccess(str.length());
                        return null;
                    });
                    return objectPromise.toCompletableResult();
                }, single)
                .addHandler(ar -> {
                    assertEquals(ar.result(), new Integer(4));
                    System.out.println("thenComposeAsync1 handler thread -> " + Thread.currentThread().getName());
                    assertFalse(Arrays.asList(mainName, "single").contains(Thread.currentThread().getName()));
                });
        promise.setSuccess("then");

        Thread.sleep(1000);
    }

    @Test
    public void exceptionally() throws InterruptedException {
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .exceptionally(err -> {
                    assertEquals(err.getMessage(), "error");
                    return err.getMessage();
                })
                .addHandler(ar -> {
                   assertEquals(ar.result(), "error");
                   assertTrue(ar.succeeded());
                });

        Thread.sleep(1000);
    }

    @Test
    public void whenComplete() {

    }

    @Test
    public void whenCompleteAsync() {
    }

    @Test
    public void whenCompleteAsync1() {
    }

    @Test
    public void handle() {

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .handle((str, err) -> {
                    assertEquals(str, "handle");
                    assertNull(err);
                    return (str + str).length();
                });
        promise.setSuccess("handle");

        Promise<String> promise1 = Async.promise();
        promise1.toCompletableResult()
                .handle((str, err) -> {
                    assertEquals(err.getMessage(), "error");
                    assertNull(str);
                    return null;
                });
        promise1.setFailure(new Error("error"));
    }

    @Test
    public void handleAsync() throws InterruptedException {
        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .handleAsync((str, err) -> {
                    System.out.println("1. handleAsync thread -> " + Thread.currentThread().getName());
                    assertEquals(str, "handle");
                    assertNull(err);
                    return (str + str).length();
                });
        promise.setSuccess("handle");

        Promise<String> promise1 = Async.promise();
        promise1.toCompletableResult()
                .handleAsync((str, err) -> {
                    System.out.println("2. handleAsync thread -> " + Thread.currentThread().getName());
                    assertEquals(err.getMessage(), "error");
                    assertNull(str);
                    return null;
                });
        promise1.setFailure(new Error("error"));

        Thread.sleep(1000);
    }

    @Test
    public void handleAsync1() {

        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .handleAsync((str, err) -> {
                    assertEquals("single", Thread.currentThread().getName());
                    System.out.println("1. handleAsync1 thread -> " + Thread.currentThread().getName());
                    assertEquals(str, "handle");
                    assertNull(err);
                    return (str + str).length();
                }, single);
        promise.setSuccess("handle");

        Promise<String> promise1 = Async.promise();
        promise1.toCompletableResult()
                .handleAsync((str, err) -> {
                    assertEquals("single", Thread.currentThread().getName());
                    System.out.println("2. handleAsync1 thread -> " + Thread.currentThread().getName());
                    assertEquals(err.getMessage(), "error");
                    assertNull(str);
                    return null;
                }, single);
        promise1.setFailure(new Error("error"));
    }

    @Test
    public void toCompletableFuture() {

        Promise<String> promise = Async.promise();
        promise.toCompletableResult()
                .toCompletableFuture()
                .complete("yes");
        promise.toFuture().addHandler(ar -> {
            assertEquals(ar.result(), "yes");
        });
    }
}