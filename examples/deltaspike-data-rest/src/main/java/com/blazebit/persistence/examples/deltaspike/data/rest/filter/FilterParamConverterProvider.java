/*
 * Copyright 2014 - 2018 Blazebit.
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
