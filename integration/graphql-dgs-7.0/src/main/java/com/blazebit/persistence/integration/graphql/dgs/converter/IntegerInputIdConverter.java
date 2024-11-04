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
 * Converter from {@link String} to {@link Integer}.
 *
 * @author Tom Mayer
 * @since 1.6.9
 */
@Component
public class IntegerInputIdConverter implements EntityViewInputIdTypeConverter<Integer> {
    @Override
    public Set<Class<?>> getSupportedClasses() {
        return new HashSet<>(Arrays.asList(Integer.class, int.class));
    }

    @Override
    public Integer convert(final String value) {
        return Integer.parseInt(value);
    }
}
