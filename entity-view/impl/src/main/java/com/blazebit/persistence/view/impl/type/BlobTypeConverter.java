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
