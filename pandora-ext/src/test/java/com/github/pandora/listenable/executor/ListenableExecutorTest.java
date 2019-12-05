package com.github.pandora.listenable.executor;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ListenableExecutorTest {

    @Test
    public void create() {
        ExecutorService single = Executors.newSingleThreadExecutor(r -> new Thread(r, "single"));

        ListenableExecutor executor = ListenableExecutor.create(single);

        executor.execute(() -> {
            Assert.assertEquals(Thread.currentThread().getName(), "single");
        });

    }

    @Test
    public void create1() {
        create();
    }
}