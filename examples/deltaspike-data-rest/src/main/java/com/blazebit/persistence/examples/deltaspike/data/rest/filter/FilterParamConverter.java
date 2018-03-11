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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.ws.rs.ext.ParamConverter;
import java.io.IOException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FilterParamConverter implements ParamConverter<Filter> {

    private final ObjectReader filterReader;
    private final ObjectWriter filterWriter;

    public FilterParamConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        filterReader = mapper.reader(Filter.class);
        filterWriter = mapper.writerWithType(Filter.class);
    }

    @Override
    public Filter fromString(String s) {
        try {
            return filterReader.readValue(s);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString(Filter filter) {
        try {
            return filterWriter.writeValueAsString(filter);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
