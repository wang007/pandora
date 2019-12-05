package com.github.pandora.asyncResult;


import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncableTest {

    private Asyncable<String> succeeded = Async.succeededFuture("");
    private Asyncable<String> failed = Async.failedFuture(new Error());
    private Asyncable<String> pending = Async.promise();

    @Test
    public void isCompleted() {
        assertTrue(succeeded.isCompleted());
        assertTrue(failed.isCompleted());
        assertFalse(pending.isCompleted());
    }

    @Test
    public void toFuture() {
        assertNotNull(succeeded.toFuture());
        assertNotNull(failed.toFuture());
        assertNotNull(pending.toFuture());
    }

    @Test
    public void toCompletionStage() {
        assertNotNull(succeeded.toFuture());
        assertNotNull(failed.toFuture());
        assertNotNull(pending.toFuture());
    }
}