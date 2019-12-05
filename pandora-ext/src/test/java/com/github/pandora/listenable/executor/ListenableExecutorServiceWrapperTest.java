package com.github.pandora.listenable.executor;

import com.github.pandora.listenable.future.ListenableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ListenableExecutorServiceWrapperTest {

    @Test
    public void submit() throws InterruptedException {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));
        ListenableExecutorService service = ListenableExecutor.create(single);
        String mainName = Thread.currentThread().getName();
        //submit Runnable在上个方法已测

        //submit Runnable,R    跟submit Callable一样，所以重点测submit Callable
        service.submit(() -> {}, "submit").addHandler(ar -> Assert.assertEquals("submit", ar.result()));

        ListenableFuture<String> future = service.submit(() -> {
            String name = Thread.currentThread().getName();
            Assert.assertEquals(name, "single");
            return "submit";
        });

        Thread.sleep(500); //等待submit提交的callable先执行完，那么addHandler 回调执行一定在main线程

        future.addHandler(ar -> {
            System.out.println("2.submit addHandler -> " + Thread.currentThread().getName());
            Assert.assertEquals(mainName, Thread.currentThread().getName());
            Assert.assertEquals("submit", ar.result());
        });


        service.submit(() -> {
            String name = Thread.currentThread().getName();
            Assert.assertEquals(name, "single");
            Thread.sleep(1000L);  //这里sleep，让下面的addHandler先执行，那么addHandler 回调执行一定在Single线程
            return "submit";
        }).addHandler(ar -> {
            System.out.println("1.submit addHandler -> " + Thread.currentThread().getName());
            Assert.assertEquals("single", Thread.currentThread().getName());
            Assert.assertEquals("submit", ar.result());
        });

        Thread.sleep(3000);

    }
}