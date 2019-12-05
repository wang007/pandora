package com.github.pandora.expDsl;

import com.github.pandora.asyncResult.Asyncable;


import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Aif == async if
 *
 * 异步化的if-elseIf-else 表达式
 *
 * created by wang007 on 2019/12/5
 */
public class Aif<T> {

    public <R> ElseIf<T> doIf(Asyncable<R> source, Predicate<T> condition) {
        return null;
    }


    abstract static class Aif0<T> extends ElseIf<T> {

    }


    abstract static class ElseIf<T> extends Else<T> implements Supplier<Asyncable<? extends T>> {

    }

    abstract static class Else<T> implements Supplier<Asyncable<? extends T>> {

        //Asyncable<? extends T> doElse(Function<T>)

    }



}
