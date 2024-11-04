/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
