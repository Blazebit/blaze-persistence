/*
 * Copyright 2014 - 2019 Blazebit.
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

import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class PrimitiveShortTypeConverter implements TypeConverter<Object, Object> {

    public static final TypeConverter<Object, Object> INSTANCE = new PrimitiveShortTypeConverter();

    private PrimitiveShortTypeConverter() {
    }

    @Override
    public Class<? extends Object> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return short.class;
    }

    @Override
    public Object convertToViewType(Object object) {
        if (object == null) {
            return (short) 0;
        }
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertToUnderlyingType(Object object) {
        return object;
    }
}


