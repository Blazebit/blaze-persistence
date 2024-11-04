/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class PrimitiveFloatTypeConverter implements TypeConverter<Object, Object> {

    public static final TypeConverter<Object, Object> INSTANCE = new PrimitiveFloatTypeConverter();

    private PrimitiveFloatTypeConverter() {
    }

    @Override
    public Class<? extends Object> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return float.class;
    }

    @Override
    public Object convertToViewType(Object object) {
        if (object == null) {
            return 0F;
        }
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToUnderlyingType(Object object) {
        return object;
    }
}


