/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.hateoas.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class FilterUriComponentsContributor implements UriComponentsContributor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Filter[].class;
    }

    @Override
    public void enhance(UriComponentsBuilder builder, MethodParameter parameter, Object value) {
        if (value != null) {
            try {
                builder.replaceQueryParam(parameter.getAnnotatedElement().getAnnotation(RequestParam.class).name(), mapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
