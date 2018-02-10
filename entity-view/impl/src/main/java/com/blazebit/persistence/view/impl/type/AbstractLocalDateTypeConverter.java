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

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractLocalDateTypeConverter<T> implements TypeConverter<T, Object> {

    protected static final long MILLISECOND_CONVERSION_FACTOR = 24 * 60 * 60 * 1000;
    private static final Method OF_EPOCH_DAY;
    private static final Method TO_EPOCH_DAY;

    static {
        Method ofEpochDay = null;
        Method toEpochDay = null;
        try {
            Class<?> c = Class.forName("java.time.LocalDate");
            ofEpochDay = c.getDeclaredMethod("ofEpochDay", long.class);
            toEpochDay = c.getDeclaredMethod("toEpochDay");
        } catch (Exception e) {
            // Ignore
        }

        OF_EPOCH_DAY = ofEpochDay;
        TO_EPOCH_DAY = toEpochDay;
    }

    protected final Object ofEpochDay(long epochDay) {
        try {
            return OF_EPOCH_DAY.invoke(null, epochDay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final long toEpochDay(Object object) {
        try {
            return (long) TO_EPOCH_DAY.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
