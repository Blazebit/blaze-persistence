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
 * A contract for a converter to convert between types of an entity and entity view model.
 *
 * @param <X> The type in the entity model
 * @param <Y> The type in the entity view model
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TypeConverter<X, Y> {

    /**
     * Extract the entity view model type from the declared type.
     * The owning class is the concrete entity view class which contains a field or method of the declared type.
     *
     * @param owningClass The class owning the declared type
     * @param declaredType The declared type as present in the entity view model
     * @return The actual entity view model type
     */
    public Class<? extends Y> getViewType(Class<?> owningClass, Type declaredType);

    /**
     * Converts the object from entity model type to the entity view model type.
     *
     * @param object The object to convert
     * @return The converted object
     */
    public Y convertToViewType(X object);

    /**
     * Converts the object from entity view model type to the entity model type.
     *
     * @param object The object to convert
     * @return The converted object
     */
    public X convertToEntityType(Y object);
}
