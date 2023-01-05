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
