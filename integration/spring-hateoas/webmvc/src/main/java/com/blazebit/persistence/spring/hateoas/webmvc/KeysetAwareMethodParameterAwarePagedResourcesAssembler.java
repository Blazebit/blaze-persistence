/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

/**
 * @author Christian Beikov
 * @since 1.5.0
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
