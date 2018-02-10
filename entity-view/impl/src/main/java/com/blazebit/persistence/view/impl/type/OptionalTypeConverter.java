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
import com.blazebit.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OptionalTypeConverter implements TypeConverter<Object, Object> {

    private static final Object[] NULL_ARRAY = new Object[] { null };
    private static final Method OF_NULLABLE;
    private static final Method OR_ELSE;

    static {
        Method ofNullable = null;
        Method orElse = null;
        try {
            Class<?> c = Class.forName("java.util.Optional");
            ofNullable = c.getDeclaredMethod("ofNullable", Object.class);
            orElse = c.getDeclaredMethod("orElse", Object.class);
        } catch (Exception e) {
            // Ignore
        }

        OF_NULLABLE = ofNullable;
        OR_ELSE = orElse;
    }

    @Override
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        if (declaredType.getClass() == Class.class) {
            return (Class<?>) declaredType;
        }
        return ReflectionUtils.resolveTypeArguments(owningClass, declaredType)[0];
    }

    @Override
    public Object convertToViewType(Object object) {
        try {
            return OF_NULLABLE.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertToUnderlyingType(Object object) {
        try {
            return OR_ELSE.invoke(object, NULL_ARRAY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
