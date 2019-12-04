package listenable;

import com.github.wang007.expDsl.Try;

import java.util.concurrent.CompletableFuture;

/**
 * created by wang007 on 2019/12/2
 */
public class Test {


    public static void main1(String[] args) throws InterruptedException {
        CompletableFuture<String> fut = new CompletableFuture<>();
        new Thread(() -> {
            fut.thenAccept(v1 -> {
                try { Thread.sleep(1000L); } catch (InterruptedException e) {}
                System.out.println("thread-name-v1 " + Thread.currentThread().getName()); });

            fut.thenAccept(v1 -> {
                try { Thread.sleep(1000L); } catch (InterruptedException e) {}
                System.out.println("thread-name-v1 " + Thread.currentThread().getName()); });

            fut.thenAccept(v1 -> {
                try { Thread.sleep(1000L); } catch (InterruptedException e) {}
                System.out.println("thread-name-v1 " + Thread.currentThread().getName()); });

            fut.thenAccept(v1 -> {
                try { Thread.sleep(1000L); } catch (InterruptedException e) {}
                System.out.println("thread-name-v1 " + Thread.currentThread().getName()); });

            fut.thenAccept(v1 -> {
                try { Thread.sleep(1000L); } catch (InterruptedException e) {}
                System.out.println("thread-name-v1 " + Thread.currentThread().getName());});

        }, "thread-1").start();

        Thread.sleep(2000L);
        new Thread(() -> fut.complete("111"), "thread-2").start();

        new Thread(() -> fut.thenAccept(str -> {}), "thread-3").start();
    }

    public static void main(String[] args) {

    }
}
