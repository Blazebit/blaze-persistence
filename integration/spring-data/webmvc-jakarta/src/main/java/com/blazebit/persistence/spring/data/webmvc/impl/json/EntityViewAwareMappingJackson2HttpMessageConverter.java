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

package com.blazebit.persistence.spring.data.webmvc.impl.json;

import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class EntityViewAwareMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private final EntityViewAwareObjectMapper entityViewAwareObjectMapper;

    public EntityViewAwareMappingJackson2HttpMessageConverter(final EntityViewManager entityViewManager, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        this.entityViewAwareObjectMapper = new EntityViewAwareObjectMapper(entityViewManager, getObjectMapper(), entityViewIdValueAccessor);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (!entityViewAwareObjectMapper.canRead(clazz)) {
            return false;
        }
        return super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        JavaType javaType = getJavaType(type, contextClass);
        if (!entityViewAwareObjectMapper.canRead(javaType)) {
            return false;
        }
        return super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) {
        try {
            if (inputMessage instanceof MappingJacksonInputMessage) {
                Class<?> deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                if (deserializationView != null) {
                    return this.getObjectMapper().readerWithView(deserializationView).forType(javaType).
                            readValue(inputMessage.getBody());
                }
            }
            return entityViewAwareObjectMapper.readerFor(javaType).readValue(inputMessage.getBody());
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document: " + ex.getMessage(), ex);
        }
    }
}
