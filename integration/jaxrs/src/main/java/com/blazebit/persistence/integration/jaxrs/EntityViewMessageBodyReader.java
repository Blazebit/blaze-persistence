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
import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Priority(Priorities.USER - 1)
@Provider
// "*/*" needs to be included since Jersey does not support the "application/*+json" notation
@Consumes({"application/json", "application/*+json", "text/json", "*/*"})
public class EntityViewMessageBodyReader implements MessageBodyReader<Object> {

    @Inject
    private Instance<EntityViewManager> entityViewManager;
    @Inject
    @Any
    private Instance<ParamConverterProvider> paramConverterProviders;
    @Context
    private UriInfo uriInfo;

    private EntityViewAwareObjectMapper entityViewAwareObjectMapper;
    private final ThreadLocal<String> idValueHolder = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        if (entityViewManager.isUnsatisfied()) {
            this.entityViewAwareObjectMapper = null;
        } else {
            this.entityViewAwareObjectMapper = new EntityViewAwareObjectMapper(entityViewManager.get(), new ObjectMapper(), new EntityViewIdValueAccessor() {
                @Override
                public <T> T getValue(JsonParser jsonParser, Class<T> idType) {
                    String value = idValueHolder.get();
                    if (value == null) {
                        return null;
                    } else {
                        ParamConverter<T> paramConverter = null;
                        for (ParamConverterProvider paramConverterProvider : paramConverterProviders) {
                            if ((paramConverter = paramConverterProvider.getConverter(idType, idType, null)) != null) {
                                break;
                            }
                        }
                        return paramConverter == null ? null : paramConverter.fromString(value);
                    }
                }
            });
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !entityViewManager.isUnsatisfied()
                && entityViewManager.get().getMetamodel().view(type) != null
                && hasMatchingMediaType(mediaType)
                && !InputStream.class.isAssignableFrom(type)
                && !Reader.class.isAssignableFrom(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        EntityViewId entityViewAnnotation = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(EntityViewId.class)) {
                entityViewAnnotation = (EntityViewId) annotation;
                break;
            }
        }
        if (entityViewAnnotation != null) {
            String pathVariableName = entityViewAnnotation.value().isEmpty() ? entityViewAnnotation.name() : entityViewAnnotation.value();
            if (pathVariableName.isEmpty()) {
                throw new IllegalArgumentException(
                        "Entity view id path param name for argument type [" + type.getName() +
                                "] not available.");
            }
            String pathVariableStringValue = uriInfo.getPathParameters().getFirst(pathVariableName);
            idValueHolder.set(pathVariableStringValue);
        }

        try {
            if (entityViewAwareObjectMapper != null && entityViewAwareObjectMapper.canRead(type)) {
                JavaType javaType = entityViewAwareObjectMapper.getObjectMapper().constructType(genericType);
                ObjectReader objectReader = entityViewAwareObjectMapper.readerFor(javaType);
                try {
                    return objectReader.readValue(entityStream);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } finally {
            idValueHolder.remove();
        }

        return null;
    }

    /**
     * Copy of {@link com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider#hasMatchingMediaType(javax.ws.rs.core.MediaType)}
     *
     * @param mediaType the media type to be matched
     * @return true, if this reader accepts the given mediaType or false otherwise
     */
    private boolean hasMatchingMediaType(MediaType mediaType) {
        /* As suggested by Stephen D, there are 2 ways to check: either
         * being as inclusive as possible (if subtype is "json"), or
         * exclusive (major type "application", minor type "json").
         * Let's start with inclusive one, hard to know which major
         * types we should cover aside from "application".
         */
        if (mediaType != null) {
            // Ok: there are also "xxx+json" subtypes, which count as well
            String subtype = mediaType.getSubtype();

            // [Issue#14]: also allow 'application/javascript'
            return "json".equalsIgnoreCase(subtype)
                    || subtype.endsWith("+json")
                    || "javascript".equals(subtype)
                    // apparently Microsoft once again has interesting alternative types?
                    || "x-javascript".equals(subtype)
                    || "x-json".equals(subtype) // [Issue#40]
                    ;
        }
        /* Not sure if this can happen; but it seems reasonable
         * that we can at least produce JSON without media type?
         */
        return true;
    }
}
