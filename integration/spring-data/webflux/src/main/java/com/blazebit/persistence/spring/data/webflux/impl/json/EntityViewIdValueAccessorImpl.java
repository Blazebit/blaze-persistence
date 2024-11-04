/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webflux.impl.json;

import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.fasterxml.jackson.core.JsonParser;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewIdValueAccessorImpl implements EntityViewIdValueAccessor {

    final Map<JsonParser, Object> entityViewIdLookupMap = new ConcurrentHashMap<>();
    private final DataBinder dataBinder;

    public EntityViewIdValueAccessorImpl(ConversionService conversionService) {
        dataBinder = new DataBinder(null);
        dataBinder.setConversionService(conversionService);
    }

    @Override
    public <T> T getValue(JsonParser jsonParser, Class<T> idType) {
        Object rawValue = entityViewIdLookupMap.get(jsonParser);
        return rawValue == null ? null : dataBinder.convertIfNecessary(rawValue, idType);
    }
}
