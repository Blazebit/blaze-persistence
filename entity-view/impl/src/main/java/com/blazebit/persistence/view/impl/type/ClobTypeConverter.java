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

import java.lang.reflect.Type;
import java.sql.Clob;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ClobTypeConverter implements TypeConverter<Clob, Clob> {

    public static final TypeConverter<Clob, Clob> INSTANCE = new ClobTypeConverter();

    private ClobTypeConverter() {
    }

    @Override
    public Class<? extends Clob> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Clob.class;
    }

    @Override
    public Clob convertToViewType(Clob object) {
        return ClobProxy.generateProxy(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Clob convertToUnderlyingType(Clob object) {
        if (object == null || !(object instanceof LobImplementor<?>)) {
            return null;
        }
        return ((LobImplementor<Clob>) object).getWrapped();
    }
}
