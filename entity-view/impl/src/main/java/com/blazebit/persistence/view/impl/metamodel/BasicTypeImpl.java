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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicTypeImpl<X> implements BasicType<X> {

    private final Class<X> javaType;
    private final Type convertedType;
    private final TypeConverter<?, X> converter;
    private final ManagedType<X> managedType;
    private final BasicUserType<X> userType;

    public BasicTypeImpl(Class<X> javaType, ManagedType<X> managedType, BasicUserType<X> userType) {
        this.javaType = javaType;
        this.convertedType = null;
        this.converter = null;
        this.managedType = managedType;
        this.userType = userType;
    }

    public BasicTypeImpl(Class<X> javaType, ManagedType<X> managedType, BasicUserType<X> userType, Type convertedType, TypeConverter<?, X> converter) {
        this.javaType = javaType;
        this.convertedType = convertedType;
        this.converter = converter;
        this.managedType = managedType;
        this.userType = userType;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public Type getConvertedType() {
        return convertedType;
    }

    @Override
    public TypeConverter<?, X> getConverter() {
        return converter;
    }

    @Override
    public MappingType getMappingType() {
        return MappingType.BASIC;
    }

    @Override
    public BasicUserType<X> getUserType() {
        return userType;
    }

    public ManagedType<X> getManagedType() {
        return managedType;
    }

    public boolean isJpaManaged() {
        return managedType != null;
    }

    public boolean isJpaEntity() {
        return managedType instanceof EntityType<?>;
    }
}
