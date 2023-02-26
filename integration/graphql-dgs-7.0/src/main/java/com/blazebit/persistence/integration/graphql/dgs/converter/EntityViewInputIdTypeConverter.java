/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.graphql.dgs.converter;

import java.util.Set;

/**
 * A contract for converting a String of the GraphQL {@code ID} type to a different type.
 *
 * @param <T> The converted type
 * @author Tom Mayer
 * @since 1.6.9
 */
public interface EntityViewInputIdTypeConverter<T> {
    /**
     * Returns the classes that this converter supports for conversion.
     *
     * @return the supported classes for conversion
     */
    Set<Class<?>> getSupportedClasses();

    /**
     * Converts the given string value to the type {@code T}.
     *
     * @param value The string value
     * @return The converted value
     */
    T convert(String value);
}
