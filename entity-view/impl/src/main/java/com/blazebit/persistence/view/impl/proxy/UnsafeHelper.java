/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.proxy;

import java.lang.reflect.Field;

//CHECKSTYLE:OFF: IllegalImport
import sun.misc.Unsafe;
//CHECKSTYLE:ON: IllegalImport

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class UnsafeHelper {

    private static final Unsafe UNSAFE;

    private UnsafeHelper() {
    }

    static {
        Field f;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> define(String name, byte[] bytes, final Class<?> declaringClass) {
        try {
            ClassLoader newLoader = declaringClass.getClassLoader();
            return UNSAFE.defineClass(name, bytes, 0, bytes.length, newLoader, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
