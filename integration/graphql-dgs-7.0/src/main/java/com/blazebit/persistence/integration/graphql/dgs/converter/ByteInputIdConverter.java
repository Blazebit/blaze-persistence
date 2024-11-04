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
 * Converter from {@link String} to {@link Byte}.
 *
 * @author Tom Mayer
 * @since 1.6.9
 */
@Component
public class ByteInputIdConverter implements EntityViewInputIdTypeConverter<Byte> {
    @Override
    public Set<Class<?>> getSupportedClasses() {
        return new HashSet<>(Arrays.asList(Byte.class, byte.class));
    }

    @Override
    public Byte convert(final String value) {
        return Byte.parseByte(value);
    }
}
