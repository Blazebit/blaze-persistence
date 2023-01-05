/*
 * Copyright 2014 - 2023 Blazebit.
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
import java.lang.reflect.Method;

//CHECKSTYLE:OFF: IllegalImport
import sun.misc.Unsafe;
//CHECKSTYLE:ON: IllegalImport

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public final class UnsafeHelper {

    private static final Unsafe UNSAFE;
    private static final Method GET_MODULE;
    private static final Method ADD_READS;
    private static final Method LOOKUP;
    private static final Method PRIVATE_LOOKUP_IN;
    private static final Method DEFINE_CLASS;

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
        Method getModule = null;
        Method addReads = null;
        Method lookup = null;
        Method privateLookupIn = null;
        Method defineClass = null;
        try {
            Class<?> moduleClass = Class.forName("java.lang.Module");
            getModule = Class.class.getMethod("getModule");
            addReads = moduleClass.getMethod("addReads", moduleClass);
            Class<?> methodHandlesClass = Class.forName("java.lang.invoke.MethodHandles");
            Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
            privateLookupIn = methodHandlesClass.getMethod("privateLookupIn", Class.class, lookupClass);
            lookup = methodHandlesClass.getMethod("lookup");
            defineClass = lookupClass.getMethod("defineClass", byte[].class);

        } catch (Exception e) {
            // ignore
        }
        GET_MODULE = getModule;
        ADD_READS = addReads;
        LOOKUP = lookup;
        PRIVATE_LOOKUP_IN = privateLookupIn;
        DEFINE_CLASS = defineClass;
    }

    public static Class<?> define(String name, byte[] bytes, final Class<?> declaringClass) {
        try {
            ClassLoader newLoader = declaringClass.getClassLoader();
            return UNSAFE.defineClass(name, bytes, 0, bytes.length, newLoader, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodError e) {
            if (ADD_READS == null) {
                throw e;
            }
            // In case of a Maven build, we might run in a class loader that refers to this module through a directory,
            // in which case we want to fall back to the JDK9+ APIs if possible
            try {
                ADD_READS.invoke(GET_MODULE.invoke(UnsafeHelper.class), GET_MODULE.invoke(declaringClass));
                return (Class<?>) DEFINE_CLASS.invoke(PRIVATE_LOOKUP_IN.invoke(null, declaringClass, LOOKUP.invoke(null)), (Object) bytes);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
