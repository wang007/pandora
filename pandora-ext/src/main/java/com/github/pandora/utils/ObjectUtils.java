package com.github.pandora.utils;

/**
 * created by pandora on 2019/12/4
 */
public class ObjectUtils {

    public static void requireNonNull(Object obj) {
        if (obj == null) throw new NullPointerException();
    }

    public static void requireNonNull(Object o1,Object o2) {
        if (o1 == null) throw new NullPointerException();
        if (o2 == null) throw new NullPointerException();
    }

    public static void requireNonNull(Object o1, Object o2, Object o3) {
        if (o1 == null) throw new NullPointerException();
        if (o2 == null) throw new NullPointerException();
        if (o3 == null) throw new NullPointerException();
    }

    public static void requireNonNull(Object o1, Object o2, Object o3, Object o4) {
        if (o1 == null) throw new NullPointerException();
        if (o2 == null) throw new NullPointerException();
        if (o3 == null) throw new NullPointerException();
        if (o4 == null) throw new NullPointerException();
    }

}
