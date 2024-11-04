/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

/**
 * A contract for converting values of one type to another as well as rendering as JPQL literal.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TypeConverter<T> {

    /**
     * Converts the given value to the type for which the converter was registered.
     *
     * @param value The value to convert.
     * @return The converted value
     * @throws IllegalArgumentException If the conversion is not possible
     */
    T convert(Object value);

    /**
     * Returns the JPQL literal representation of the given value as string.
     *
     * @param value The value
     * @return The JPQL literal
     */
    String toString(T value);

    /**
     * Appends the JPQL literal representation of the given value to the given string builder.
     *
     * @param value The value
     * @param stringBuilder The string builder
     */
    void appendTo(T value, StringBuilder stringBuilder);
}