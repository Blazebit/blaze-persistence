/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static List<Method> getSetters(Class<?> clazz, String fieldName) {
        final String internedName = ("set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)).intern();
        final List<Method> setters = new ArrayList<>(1);
        traverseHierarchy(clazz, new TraverseTask<Method>() {

            @Override
            public Method run(Class<?> clazz) {
                Method[] methods = clazz.getDeclaredMethods();
                Method res = null;

                for (int i = 0; i < methods.length; i++) {
                    Method m = methods[i];
                    if (isSetterSignature(m)) {
                        if (m.getName() == internedName
                                && (res == null
                                || res.getParameterTypes()[0].isAssignableFrom(m.getParameterTypes()[0]))) {
                            res = m;
                        }
                    }
                }

                if (res != null) {
                    if (!setters.isEmpty()) {
                        ListIterator<Method> listIterator = setters.listIterator();
                        while (listIterator.hasNext()) {
                            Method m = listIterator.next();
                            if (m.getParameterTypes()[0].isAssignableFrom(res.getParameterTypes()[0])) {
                                listIterator.set(res);
                                return null;
                            }
                        }
                    }
                    setters.add(res);
                }
                return null;
            }
        });
        return setters;
    }

    private static boolean isSetterSignature(Method m) {
        return m != null && m.getReturnType().equals(void.class)
                && m.getParameterTypes().length == 1;
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    private static interface TraverseTask<T> {
        public T run(Class<?> clazz);
    }

    private static <T> T traverseHierarchy(Class<?> clazz, TraverseTask<T> task) {
        Queue<Class<?>> classQueue = new LinkedList<Class<?>>();
        Class<?> traverseClass;
        classQueue.add(clazz);

        while (!classQueue.isEmpty()) {
            traverseClass = classQueue.remove();

            T result = task.run(traverseClass);

            if (result != null) {
                return result;
            }

            if (traverseClass.getSuperclass() != null) {
                classQueue.add(traverseClass.getSuperclass());
            }

            for (Class<?> interfaceClass : traverseClass.getInterfaces()) {
                classQueue.add(interfaceClass);
            }
        }

        return null;
    }
}
