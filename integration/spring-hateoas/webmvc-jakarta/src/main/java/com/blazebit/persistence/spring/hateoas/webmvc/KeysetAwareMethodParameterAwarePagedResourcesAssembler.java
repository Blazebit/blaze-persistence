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

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
class KeysetAwareMethodParameterAwarePagedResourcesAssembler<T> extends KeysetAwarePagedResourcesAssembler<T> {

    private final MethodParameter parameter;

    public KeysetAwareMethodParameterAwarePagedResourcesAssembler(MethodParameter parameter, HateoasKeysetPageableHandlerMethodArgumentResolver resolver, UriComponents baseUri, ObjectMapper objectMapper) {
        super(resolver, baseUri, objectMapper);

        Assert.notNull(parameter, "Method parameter must not be null!");
        this.parameter = parameter;
    }

    @Override
    protected MethodParameter getMethodParameter() {
        return parameter;
    }
}
