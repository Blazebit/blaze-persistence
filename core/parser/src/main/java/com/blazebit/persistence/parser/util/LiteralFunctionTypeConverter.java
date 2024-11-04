/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

/**
 * A special type converter that supports rendering a type value through a literal function.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface LiteralFunctionTypeConverter<T> extends TypeConverter<T> {

    /**
     * Returns the name of the literal function to use.
     *
     * @return The nane of the literal function
     */
    String getLiteralFunctionName();

}