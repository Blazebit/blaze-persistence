/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.jaxrs;

import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Provider
public class EntityViewParamConverterProvider implements ParamConverterProvider {

    @Context
    private UriInfo requestUriInfo;
    @Inject
    private Instance<EntityViewManager> entityViewManager;

    private EntityViewAwareObjectMapper entityViewAwareObjectMapper;

    @PostConstruct
    public void init() {
        if (entityViewManager.isUnsatisfied()) {
            this.entityViewAwareObjectMapper = null;
        } else {
            this.entityViewAwareObjectMapper = new EntityViewAwareObjectMapper(entityViewManager.get(), new ObjectMapper());
        }
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        if (entityViewAwareObjectMapper != null && entityViewAwareObjectMapper.canRead(rawType)) {
            return (ParamConverter<T>) new EntityViewParamConverter(entityViewAwareObjectMapper.readerFor(rawType));
        }

        return null;
    }

}
