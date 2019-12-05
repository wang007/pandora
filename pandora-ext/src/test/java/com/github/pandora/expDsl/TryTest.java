package com.github.pandora.expDsl;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TryTest {

    private static final RuntimeException Rt = new RuntimeException("rt");

    @Test
    public void succeededTry() {
        assertNotNull(Try.succeededTry(""));
    }

    @Test
    public void failedTry() {
        assertNotNull(Try.failedTry(new Error()));
    }

    @Test
    public void doTry() {
        Try.doTry(() -> "try").addHandler(ar -> {
            assertEquals(ar.result(), "try");
        });

        Try.doTry(() -> {
            throw new Error();
        }).addHandler(ar -> {
            assertTrue(ar.cause() instanceof Error);
        });
    }

    @Test
    public void toOptional() {
        Try<Object> succeededTry = Try.succeededTry(null);
        Try<String> succeededTry1 = Try.succeededTry("try");
        Try<Object> failedTry = Try.failedTry(Rt);

        Assert.assertEquals(succeededTry.toOptional().orElse("aaa"), "aaa");
        Assert.assertEquals(failedTry.toOptional().orElse("aaa"), "aaa");
        Assert.assertEquals(succeededTry1.toOptional().get(), "try");
    }

    @Test
    public void getOrElse() {
        Try<String> succeededTry = Try.succeededTry("try");
        Assert.assertEquals("try", succeededTry.get());
        Assert.assertEquals("try", succeededTry.getOrElse("sss"));
        Assert.assertEquals("try", succeededTry.orElse(Try.succeededTry("sss")));

        Try<Object> failedTry = Try.failedTry(Rt);
        try {
            failedTry.get();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        Assert.assertEquals("failed", failedTry.getOrElse("failed"));
        Assert.assertEquals("failed", failedTry.orElse(Try.succeededTry("failed")));

        try {
            failedTry.orElse(Try.failedTry(new Error()));
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

    }

    @Test
    public void orElse() {
        getOrElse();
    }

    @Test
    public void get() {
        getOrElse();
    }

    @Test
    public void addHandler() {
        doTry();
    }

    @Test
    public void flatMap() {
        Try<String> succeededTry = Try.succeededTry("try");
        Integer result = succeededTry.flatMap(str -> {
            return Try.succeededTry(str.length());
        }).result();

        Assert.assertEquals(result, new Integer(3));

        Try<Object> objectTry = Try.failedTry(Rt);
        Throwable cause = objectTry.flatMap(obj -> {
            return Try.succeededTry(obj.toString());
        }).cause();
        Assert.assertEquals(Rt, cause);

        Try<String> succeededTry1 = Try.succeededTry("try");
        String msg = succeededTry1
                .flatMap(str -> {
                    if (str.length() == 3) throw new RuntimeException("throw");
                    return Try.succeededTry(str.length());
                })
                .map(len -> len + "" + len)
                .cause().getMessage();
        Assert.assertEquals(msg, "throw");
    }

    @Test
    public void map() {
        Try<String> succeededTry = Try.succeededTry("try");
        Integer result = succeededTry.map(str -> {
            return str.length();
        }).result();

        Assert.assertEquals(result, new Integer(3));

        Try<Object> objectTry = Try.failedTry(Rt);
        Try<String> map = objectTry.map(obj -> {
            return obj.toString();
        });
        Assert.assertEquals(map.cause(), Rt);

        Try<String> succeededTry1 = Try.succeededTry("try");
        String msg = succeededTry1.map(str -> {
            if (str.length() == 3) throw new RuntimeException("throw");
            return str.length();
        }).cause().getMessage();
        Assert.assertEquals(msg, "throw");
    }

    @Test
    public void map1() {
        map();
    }

    @Test
    public void otherwise() {
        Try<String> succeededTry = Try.succeededTry("try");
        String result = succeededTry.otherwise(err -> {
            return err.getMessage();
        }).result();
        Assert.assertEquals(result, "try");

        Try<Object> objectTry = Try.failedTry(Rt);
        Object result1 = objectTry.otherwise(Throwable::getMessage).result();
        Assert.assertEquals(result1, "rt");
    }

    @Test
    public void otherwise1() {
        otherwise();
    }
}