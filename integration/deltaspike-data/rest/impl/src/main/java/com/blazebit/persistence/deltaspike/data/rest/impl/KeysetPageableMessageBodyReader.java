/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.rest.impl;

import com.blazebit.persistence.deltaspike.data.Pageable;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Provider
public class KeysetPageableMessageBodyReader implements MessageBodyReader<Pageable> {

    @Inject
    private KeysetPageableParamConverterProvider paramConverterProvider;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Pageable.class.isAssignableFrom(type);
    }

    @Override
    public Pageable readFrom(Class<Pageable> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return paramConverterProvider.getConverter(type, genericType, annotations).fromString(null);
    }
}
