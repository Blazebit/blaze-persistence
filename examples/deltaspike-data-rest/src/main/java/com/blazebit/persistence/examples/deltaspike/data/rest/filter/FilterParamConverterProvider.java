/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest.filter;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Provider
public class FilterParamConverterProvider implements ParamConverterProvider {

    private static final ParamConverter<Filter> FILTER_CONVERTER = new FilterParamConverter();

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        if (rawType == Filter.class) {
            return (ParamConverter<T>) FILTER_CONVERTER;
        }
        return null;
    }

}
