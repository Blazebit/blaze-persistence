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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OptionalIntTypeConverter implements TypeConverter<Object, Object> {

    private static final Method OF;
    private static final Method GET_AS_INT;
    private static final Method IS_PRESENT;
    private static final Object EMPTY;

    static {
        Method of = null;
        Method getAsInt = null;
        Method isPresent = null;
        Object empty = null;
        try {
            Class<?> c = Class.forName("java.util.OptionalInt");
            of = c.getDeclaredMethod("of", int.class);
            getAsInt = c.getDeclaredMethod("getAsInt");
            isPresent = c.getMethod("isPresent");
            empty = c.getMethod("empty").invoke(null);
        } catch (Exception e) {
            // Ignore
        }

        OF = of;
        GET_AS_INT = getAsInt;
        IS_PRESENT = isPresent;
        EMPTY = empty;
    }

    @Override
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Integer.class;
    }

    @Override
    public Object convertToViewType(Object object) {
        if (object == null) {
            return EMPTY;
        }
        try {
            return OF.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertToUnderlyingType(Object object) {
        try {
            if (object == null || !((boolean) IS_PRESENT.invoke(object))) {
                return null;
            }
            return GET_AS_INT.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
