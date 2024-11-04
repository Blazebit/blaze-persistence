/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.proxy;

import java.lang.invoke.MethodHandles;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class UnsafeHelper {

    private UnsafeHelper() {
    }

    public static Class<?> define(String name, byte[] bytes, final Class<?> declaringClass) {
        try {
            UnsafeHelper.class.getModule().addReads(declaringClass.getModule());
            return MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup()).defineClass(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
