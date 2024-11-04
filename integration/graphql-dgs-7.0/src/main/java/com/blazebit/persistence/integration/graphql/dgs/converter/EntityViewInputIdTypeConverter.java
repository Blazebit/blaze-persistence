/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
