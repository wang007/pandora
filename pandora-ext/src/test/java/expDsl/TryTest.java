package expDsl;

import com.github.wang007.expDsl.Try;
import org.junit.Assert;
import org.junit.Test;

/**
 * created by wang007 on 2019/12/4
 */
public class TryTest {

    private static final RuntimeException Rt = new RuntimeException("rt");

    @Test
    public void createTest() {
        Try<String> succeededTry = Try.succeededTry("str");
        Assert.assertTrue(succeededTry.succeeded());
        Assert.assertEquals("str", succeededTry.result());

        Try<Object> failedTry = Try.failedTry(new Error());
        Assert.assertTrue(failedTry.failed());
        Assert.assertTrue(failedTry.cause() instanceof Error);


        Try<String> try0 = Try.doTry(() -> {
            return "try";
        });
        Assert.assertTrue(try0.succeeded());
        Assert.assertEquals("try", try0.result());

        Try<Object> objectTry = Try.doTry(() -> {
            throw new Error();
        });
        Assert.assertTrue(objectTry.failed());
        Assert.assertTrue(objectTry.cause() instanceof Error);
    }

    @Test
    public void getTest() {
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
    public void resultTest() {

        Try<String> succeededTry = Try.succeededTry("str");
        Assert.assertTrue(succeededTry.succeeded());
        Assert.assertFalse(succeededTry.failed());

        Assert.assertEquals("str", succeededTry.result());
        try {
            Throwable cause = succeededTry.cause();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }

        //==============


        Try<Object> failedTry = Try.failedTry(Rt);
        Assert.assertFalse(failedTry.succeeded());
        Assert.assertTrue(failedTry.failed());

        Assert.assertEquals(Rt, failedTry.cause());
        try {
            failedTry.result();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void toOptionalTest() {

        Try<Object> succeededTry = Try.succeededTry(null);
        Try<String> succeededTry1 = Try.succeededTry("try");
        Try<Object> failedTry = Try.failedTry(Rt);

        Assert.assertEquals(succeededTry.toOptional().orElse("aaa"), "aaa");
        Assert.assertEquals(failedTry.toOptional().orElse("aaa"), "aaa");
        Assert.assertEquals(succeededTry1.toOptional().get(), "try");
    }

    @Test
    public void mapTest() {
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
    public void flatMapTest() {
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
    public void otherwiseTest() {
        Try<String> succeededTry = Try.succeededTry("try");
        String result = succeededTry.otherwise(err -> {
            return err.getMessage();
        }).result();
        Assert.assertEquals(result, "try");

        Try<Object> objectTry = Try.failedTry(Rt);
        Object result1 = objectTry.otherwise(Throwable::getMessage).result();
        Assert.assertEquals(result1, "rt");

    }


}
