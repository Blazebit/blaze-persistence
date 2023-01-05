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
