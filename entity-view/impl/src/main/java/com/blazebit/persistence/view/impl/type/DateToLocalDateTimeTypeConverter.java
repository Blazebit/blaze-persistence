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

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class DateToLocalDateTimeTypeConverter<T extends Date> extends AbstractLocalDateTimeTypeConverter<T> {


    public static final DateToLocalDateTimeTypeConverter<Date> JAVA_UTIL_DATE_CONVERTER = new DateToLocalDateTimeTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Date.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Date(toEpochMillis(object));
        }

    };

    public static final DateToLocalDateTimeTypeConverter<Date> JAVA_SQL_TIMESTAMP_CONVERTER = new DateToLocalDateTimeTypeConverter<Date>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Timestamp.class;
        }

        @Override
        public Date convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            return new Timestamp(toEpochMillis(object));
        }

    };

    @Override
    public Object convertToViewType(Date object) {
        if (object == null) {
            return null;
        }
        return ofEpochMilli(object.getTime());
    }
}
