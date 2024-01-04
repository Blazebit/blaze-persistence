/*
 * Copyright 2014 - 2024 Blazebit.
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
