/*
 * Copyright 2014 - 2022 Blazebit.
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

/**
 * Like OptionalUtil from DeltaSpike but with a slightly different implementation.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public final class OptionalUtils {
    private static final Class<?> OPTIONAL_CLASS;
    private static final Method OPTIONAL_METHOD;

    static {
        Class<?> optionalClass;
        Method optionalMethod;
        try {
            optionalClass = Class.forName("java.util.Optional");
            optionalMethod = optionalClass.getMethod("ofNullable", Object.class);
        } catch (Exception e) {
            optionalClass = null;
            optionalMethod = null;
        }

        OPTIONAL_CLASS = optionalClass;
        OPTIONAL_METHOD = optionalMethod;
    }

    private OptionalUtils() {
    }

    public static boolean isOptionalReturned(Method method) {
        return OPTIONAL_CLASS != null && OPTIONAL_CLASS.isAssignableFrom(method.getReturnType());
    }

    public static Object wrap(Object input) {
        if (OPTIONAL_CLASS == null) {
            return input;
        }
        try {
            return OPTIONAL_METHOD.invoke(null, input);
        } catch (Exception e) {
            // Impossible
        }
        return null;
    }
}
