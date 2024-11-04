/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BasicUserTypeRegistry {

    public <X, Y> void registerTypeConverter(Class<X> entityModelType, Class<Y> viewModelType, TypeConverter<X, Y> converter);

    public Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> getTypeConverters();

    public <Y> Map<Class<?>, TypeConverter<?, Y>> getTypeConverter(Class<Y> clazz);

    public <X> void registerBasicUserType(Class<X> clazz, BasicUserType<X> userType);

    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes();

    public <X> BasicUserType<X> getBasicUserType(Class<X> clazz);
}
