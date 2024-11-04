/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Type;
import java.sql.NClob;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NClobTypeConverter implements TypeConverter<NClob, NClob> {

    public static final TypeConverter<NClob, NClob> INSTANCE = new NClobTypeConverter();

    private NClobTypeConverter() {
    }

    @Override
    public Class<? extends NClob> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return NClob.class;
    }

    @Override
    public NClob convertToViewType(NClob object) {
        return ClobProxy.generateProxy(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NClob convertToUnderlyingType(NClob object) {
        if (object == null || !(object instanceof LobImplementor<?>)) {
            return null;
        }
        return ((LobImplementor<NClob>) object).getWrapped();
    }
}
