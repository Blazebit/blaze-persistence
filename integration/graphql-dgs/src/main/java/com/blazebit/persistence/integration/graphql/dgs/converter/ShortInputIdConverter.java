/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.converter;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Converter from {@link String} to {@link Short}.
 *
 * @author Tom Mayer
 * @since 1.6.9
 */
@Component
public class ShortInputIdConverter implements EntityViewInputIdTypeConverter<Short> {
    @Override
    public Set<Class<?>> getSupportedClasses() {
        return new HashSet<>(Arrays.asList(Short.class, short.class));
    }

    @Override
    public Short convert(final String value) {
        return Short.parseShort(value);
    }
}
