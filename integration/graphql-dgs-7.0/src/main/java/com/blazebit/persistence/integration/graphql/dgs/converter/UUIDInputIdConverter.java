/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.converter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Converter from {@link String} to {@link UUID}.
 *
 * @author Tom Mayer
 * @since 1.6.9
 */
@Component
public class UUIDInputIdConverter implements EntityViewInputIdTypeConverter<UUID> {

    @Override
    public Set<Class<?>> getSupportedClasses() {
        return Collections.singleton(UUID.class);
    }

    @Override
    public UUID convert(final String value) {
        return UUID.fromString(value);
    }
}
