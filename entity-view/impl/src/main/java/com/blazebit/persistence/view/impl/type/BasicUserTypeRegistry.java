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
