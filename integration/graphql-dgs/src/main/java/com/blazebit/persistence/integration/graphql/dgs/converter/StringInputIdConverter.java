/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.converter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Converter from {@link String} to {@link String}.
 *
 * @author Tom Mayer
 * @since 1.6.9
 */
@Component
public class StringInputIdConverter implements EntityViewInputIdTypeConverter<String> {

    @Override
    public Set<Class<?>> getSupportedClasses() {
        return Collections.singleton(String.class);
    }

    @Override
    public String convert(final String value) {
        return value;
    }
}
