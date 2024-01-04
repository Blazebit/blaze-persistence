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

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.MethodLinkBuilderFactory;
import org.springframework.hateoas.server.core.MethodParameters;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class KeysetAwarePagedResourcesAssemblerArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeysetAwarePagedResourcesAssemblerArgumentResolver.class);

    private static final String SUPERFLOUS_QUALIFIER = "Found qualified {} parameter, but a unique unqualified {} parameter. Using that one, but you might want to check your controller method configuration!";
    private static final String PARAMETER_AMBIGUITY = "Discovered multiple parameters of type Pageable but no qualifier annotations to disambiguate!";

    private final HateoasKeysetPageableHandlerMethodArgumentResolver resolver;
    private final MethodLinkBuilderFactory<?> linkBuilderFactory;
    private final ObjectMapper objectMapper;

    public KeysetAwarePagedResourcesAssemblerArgumentResolver(HateoasKeysetPageableHandlerMethodArgumentResolver resolver, MethodLinkBuilderFactory<?> linkBuilderFactory, ObjectMapper objectMapper) {
        this.resolver = resolver;
        this.linkBuilderFactory = linkBuilderFactory == null ? new WebMvcLinkBuilderFactory() : linkBuilderFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return KeysetAwarePagedResourcesAssembler.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        UriComponents fromUriString = resolveBaseUri(parameter, webRequest);
        MethodParameter pageableParameter = findMatchingPageableParameter(parameter);

        if (pageableParameter != null) {
            return new KeysetAwareMethodParameterAwarePagedResourcesAssembler<>(pageableParameter, resolver, fromUriString, objectMapper);
        } else {
            return new KeysetAwarePagedResourcesAssembler<>(resolver, fromUriString, objectMapper);
        }
    }

    private UriComponents resolveBaseUri(MethodParameter parameter, NativeWebRequest webRequest) {
        Method method = parameter.getMethod();

        if (method == null) {
            throw new IllegalArgumentException(String.format("Could not obtain method from parameter %s!", parameter));
        }

        try {
            Link linkToMethod = linkBuilderFactory.linkTo(parameter.getDeclaringClass(), method, new Object[method.getParameterCount()]).withSelfRel();
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linkToMethod.getHref());
            for (Map.Entry<String, String[]> entry : webRequest.getParameterMap().entrySet()) {
                builder.queryParam(entry.getKey(), (Object[]) entry.getValue());
            }

            return builder.build();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static MethodParameter findMatchingPageableParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();

        if (method == null) {
            throw new IllegalArgumentException(String.format("Could not obtain method from parameter %s!", parameter));
        }

        MethodParameters parameters = MethodParameters.of(method);
        List<MethodParameter> pageableParameters = parameters.getParametersOfType(KeysetPageable.class);
        Qualifier assemblerQualifier = parameter.getParameterAnnotation(Qualifier.class);

        if (pageableParameters.isEmpty()) {
            return null;
        }

        if (pageableParameters.size() == 1) {
            MethodParameter pageableParameter = pageableParameters.get(0);
            MethodParameter matchingParameter = returnIfQualifiersMatch(pageableParameter, assemblerQualifier);

            if (matchingParameter == null) {
                LOGGER.info(SUPERFLOUS_QUALIFIER, KeysetAwarePagedResourcesAssembler.class.getSimpleName(), KeysetPageable.class.getName());
            }

            return pageableParameter;
        }

        if (assemblerQualifier == null) {
            throw new IllegalStateException(PARAMETER_AMBIGUITY);
        }

        for (MethodParameter pageableParameter : pageableParameters) {
            MethodParameter matchingParameter = returnIfQualifiersMatch(pageableParameter, assemblerQualifier);

            if (matchingParameter != null) {
                return matchingParameter;
            }
        }

        throw new IllegalStateException(PARAMETER_AMBIGUITY);
    }

    private static MethodParameter returnIfQualifiersMatch(MethodParameter pageableParameter, Qualifier assemblerQualifier) {
        if (assemblerQualifier == null) {
            return pageableParameter;
        }

        Qualifier pageableParameterQualifier = pageableParameter.getParameterAnnotation(Qualifier.class);

        if (pageableParameterQualifier == null) {
            return null;
        }

        return pageableParameterQualifier.value().equals(assemblerQualifier.value()) ? pageableParameter : null;
    }
}
