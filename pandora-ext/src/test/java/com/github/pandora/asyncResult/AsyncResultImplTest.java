package com.github.pandora.asyncResult;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncResultImplTest {

    private static final RuntimeException Rt = new RuntimeException("rt");

    @Test
    public void succeeded() {
        //ignore,result方法已覆盖
    }

    @Test
    public void failed() {
        //ignore,result方法已覆盖
    }

    @Test
    public void result() {
        AsyncResult<String> succeeded = AsyncResult.succeeded("str");
        Assert.assertTrue(succeeded.succeeded());
        Assert.assertFalse(succeeded.failed());
        Assert.assertEquals(succeeded.result(), "str");
        Assert.assertNull(succeeded.cause());

        AsyncResult<Object> failed = AsyncResult.failed(Rt);
        Assert.assertTrue(failed.failed());
        Assert.assertFalse(failed.succeeded());
        Assert.assertEquals(failed.cause(), Rt);
        Assert.assertNull(failed.result());
    }

    @Test
    public void succeeded1() {
        //ignore,result方法已覆盖
    }

    @Test
    public void cause() {
        //ignore,result方法已覆盖
    }


    //==================================


    @Test
    public void map() {
        AsyncResult<String> succeeded = AsyncResult.succeeded("str");
        AsyncResult<String> map = succeeded.map(String::length)
                .map(len -> len + "" + len);
        Assert.assertEquals(map.result(), "33");
        Assert.assertNull(map.cause());
        Assert.assertTrue(map.succeeded());


        AsyncResult<Object> failed = AsyncResult.failed(Rt);
        AsyncResult<Integer> map1 = failed.map(obj -> obj.toString()).map(String::length);
        Assert.assertTrue(map1.failed());
        Assert.assertFalse(map1.succeeded());
        Assert.assertEquals(map1.cause(), Rt);
        Assert.assertNull(map1.result());
    }


    @Test
    public void otherwise() {
        AsyncResult<String> succeeded = AsyncResult.succeeded("str");
        String result = succeeded.otherwise(Throwable::getMessage).result();
        Assert.assertEquals(result, "str");

        AsyncResult<String> failed = AsyncResult.failed(Rt);
        String result1 = failed.otherwise(Throwable::getMessage).result();
        Assert.assertEquals(result1, "rt");
    }


}