/*
 * Copyright 2014 - 2023 Blazebit.
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
