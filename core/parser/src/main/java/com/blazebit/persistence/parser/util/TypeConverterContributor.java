/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import java.util.Map;

/**
 * A service provider interface for contributing type converters.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface TypeConverterContributor {

    /**
     * Registers the converters into the given map of converters.
     *
     * @param converters The map of converters
     */
    void registerTypeConverters(Map<Class<?>, TypeConverter<?>> converters);

    /**
     * Returns a priority(lower means higher priority) of the contributor.
     *
     * @return the priority
     */
    int priority();
}