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

import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class AbstractInstantTypeConverter<T> implements TypeConverter<T, Object> {

    private static final Method OF_EPOCH_MILLI;
    private static final Method TO_EPOCH_MILLI;

    static {
        Method ofEpochMilli = null;
        Method toEpochMilli = null;
        try {
            Class<?> instantClass = Class.forName("java.time.Instant");
            ofEpochMilli = instantClass.getMethod("ofEpochMilli", long.class);
            toEpochMilli = instantClass.getMethod("toEpochMilli");
        } catch (Exception e) {
            // Ignore
        }

        OF_EPOCH_MILLI = ofEpochMilli;
        TO_EPOCH_MILLI = toEpochMilli;
    }

    protected final Object ofEpochMilli(long milli) {
        try {
            return OF_EPOCH_MILLI.invoke(null, milli);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final long toEpochMillis(Object object) {
        try {
            return (long) TO_EPOCH_MILLI.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
