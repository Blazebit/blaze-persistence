/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazebit.persistence.view.impl.proxy;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressWarnings("restriction")
public class UnsafeHelper {

    private static final Unsafe unsafe;

    static {
        Field f;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getOffset(Field field) {
        try {
            return unsafe.objectFieldOffset(field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
