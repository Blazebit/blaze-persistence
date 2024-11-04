/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.analysis;

import javassist.bytecode.analysis.Type;

import java.lang.reflect.Method;

/**
 * Utility to access {@link Type#popChanged()} reflectively which is not visible unfortunately.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
class TypeUtils {

    private static final Method POP_CHANGED;

    static {
        try {
            Method popChanged = Type.class.getDeclaredMethod("popChanged");
            popChanged.setAccessible(true);
            POP_CHANGED = popChanged;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TypeUtils() {
    }

    public static boolean popChanged(Type t) {
        try {
            return (boolean) POP_CHANGED.invoke(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
