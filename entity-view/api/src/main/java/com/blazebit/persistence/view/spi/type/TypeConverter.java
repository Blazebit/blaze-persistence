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

package com.blazebit.persistence.view.spi.type;

import java.lang.reflect.Type;

/**
 * A contract for a converter to convert between an entity view model type and an underlying type.
 *
 * @param <X> The underlying type supported by the entity view type system
 * @param <Y> The type in the entity view model for which this converter adds support
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TypeConverter<X, Y> {

    /**
     * Extract the underlying type from the declared type.
     * The owning class is the concrete entity view class which contains a field or method of the declared type.
     *
     * @param owningClass The class owning the declared type
     * @param declaredType The declared type as present in the entity view model
     * @return The actual underlying type
     */
    public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType);

    /**
     * Converts the object from underlying type to the entity view model type.
     *
     * @param object The object to convert
     * @return The converted object
     */
    public Y convertToViewType(X object);

    /**
     * Converts the object from entity view model type to the underlying type.
     *
     * @param object The object to convert
     * @return The converted object
     */
    public X convertToUnderlyingType(Y object);
}
