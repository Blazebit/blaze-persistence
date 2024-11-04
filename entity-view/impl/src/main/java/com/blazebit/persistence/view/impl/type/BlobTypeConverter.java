/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.lang.reflect.Type;
import java.sql.Blob;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlobTypeConverter implements TypeConverter<Blob, Blob> {

    public static final TypeConverter<Blob, Blob> INSTANCE = new BlobTypeConverter();

    private BlobTypeConverter() {
    }

    @Override
    public Class<? extends Blob> getUnderlyingType(Class<?> owningClass, Type declaredType) {
        return Blob.class;
    }

    @Override
    public Blob convertToViewType(Blob object) {
        return BlobProxy.generateProxy(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Blob convertToUnderlyingType(Blob object) {
        if (object == null || !(object instanceof LobImplementor<?>)) {
            return object;
        }
        return ((LobImplementor<Blob>) object).getWrapped();
    }
}
