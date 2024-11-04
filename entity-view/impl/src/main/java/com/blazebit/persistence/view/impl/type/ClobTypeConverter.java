/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
