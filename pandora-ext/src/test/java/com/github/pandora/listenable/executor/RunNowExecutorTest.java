package com.github.pandora.listenable.executor;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RunNowExecutorTest {

    @Test
    public void execute() {
        String mainName = Thread.currentThread().getName();
        RunNowExecutor.Executor.execute(() -> {
            assertEquals(mainName, Thread.currentThread().getName());
        });
    }
}