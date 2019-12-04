package com.github.wang007.expDsl;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * //TODO
 *
 * created by wang007 on 2019/12/4
 */
public class Try {


    public static <T> Try0<T> doTry(Supplier<T> tryBlock) {
        return new Try1<>(tryBlock);
    }




    public interface Finally0<T> {
        Runnable doFinally(Consumer<T> finallyBlock);
    }

    interface Finally<T> extends Runnable, Finally0<T> {}

    public interface Catch0<T> extends Finally0<T> {

        Catch<T> doCatch(Class<? extends Throwable> errClz, Function<Throwable, T> fn);

    }

    public interface Catch<T> extends Finally<T> {

        Catch<T> doCatch(Class<? extends Throwable> errClz, Function<Throwable, T> fn);
    }

    public abstract static class Try0<T> implements Catch0<T> {

         final Supplier<T> tryBlock;

         Try0(Supplier<T> tryBlock) {
            this.tryBlock = tryBlock;
        }
    }

    public static class Try1<T> extends Try0<T> implements Catch<T> {

        Try1(Supplier<T> tryBlock) {
            super(tryBlock);
        }

        @Override
        public Runnable doFinally(Consumer<T> finallyBlock) {
            return null;
        }

        @Override
        public Catch<T> doCatch(Class<? extends Throwable> errClz, Function<Throwable, T> fn) {
            return null;
        }

        @Override
        public void run() {

        }
    }



}


