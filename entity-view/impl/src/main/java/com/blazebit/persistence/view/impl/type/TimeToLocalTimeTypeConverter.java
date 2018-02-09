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
import java.sql.Time;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TimeToLocalTimeTypeConverter implements TypeConverter<Time, Object> {

    private static final Method TO_LOCAL_TIME;
    private static final Method VALUE_OF;

    static {
        Method toLocalTime = null;
        Method valueOf = null;
        try {
            Class<?> c = java.sql.Time.class;
            Class<?> localTimeClass = Class.forName("java.time.LocalTime");
            toLocalTime = c.getMethod("toLocalTime");
            valueOf = c.getMethod("valueOf", localTimeClass);
        } catch (Exception e) {
            // Ignore
        }

        TO_LOCAL_TIME = toLocalTime;
        VALUE_OF = valueOf;
    }

    @Override
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Time.class;
    }

    @Override
    public Object convertToViewType(Time object) {
        if (object == null) {
            return null;
        }
        try {
            return VALUE_OF.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Time convertToUnderlyingType(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return (Time) TO_LOCAL_TIME.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
