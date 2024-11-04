/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webmvc.impl.json;

import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.fasterxml.jackson.core.JsonParser;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewIdValueHolder implements EntityViewIdValueAccessor {

    final ThreadLocal<Object> value = new ThreadLocal<>();
    private final DataBinder dataBinder;

    public EntityViewIdValueHolder(ConversionService conversionService) {
        dataBinder = new DataBinder(null);
        dataBinder.setConversionService(conversionService);
    }

    @Override
    public <T> T getValue(JsonParser jsonParser, Class<T> idType) {
        Object rawValue = value.get();
        return rawValue == null ? null : dataBinder.convertIfNecessary(rawValue, idType);
    }
}
