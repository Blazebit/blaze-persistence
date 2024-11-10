/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.util;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Like StreamUtil from DeltaSpike but with a slightly different implementation.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public final class StreamUtils {
    private static final Class<?> STREAM_CLASS;
    private static final Method STREAM_METHOD;

    static {
        Class<?> streamClass;
        Method streamMethod;
        try {
            streamClass = Class.forName("java.util.stream.Stream");
            streamMethod = Collection.class.getMethod("stream");
        } catch (Exception e) {
            streamClass = null;
            streamMethod = null;
        }

        STREAM_CLASS = streamClass;
        STREAM_METHOD = streamMethod;
    }

    private StreamUtils() {
    }

    public static boolean isStreamReturned(Method method) {
        return STREAM_CLASS != null && STREAM_CLASS.isAssignableFrom(method.getReturnType());
    }

    public static Object wrap(Object input) {
        if (STREAM_CLASS == null || input == null) {
            return input;
        }
        try {
            return STREAM_METHOD.invoke(input);
        } catch (Exception e) {
            // Impossible
        }
        return null;
    }
}
