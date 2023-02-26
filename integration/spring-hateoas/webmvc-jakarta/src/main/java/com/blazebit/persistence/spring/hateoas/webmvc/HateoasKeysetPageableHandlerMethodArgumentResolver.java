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

import com.blazebit.persistence.spring.data.webmvc.impl.KeysetPageableHandlerMethodArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class HateoasKeysetPageableHandlerMethodArgumentResolver extends KeysetPageableHandlerMethodArgumentResolver implements UriComponentsContributor {

    private static final HateoasSortHandlerMethodArgumentResolver DEFAULT_SORT_RESOLVER = new HateoasSortHandlerMethodArgumentResolver();

    public HateoasKeysetPageableHandlerMethodArgumentResolver() {
        super(DEFAULT_SORT_RESOLVER, null, null);
    }

    public HateoasKeysetPageableHandlerMethodArgumentResolver(HateoasSortHandlerMethodArgumentResolver sortResolver, ConversionService conversionService) {
        this(sortResolver, conversionService, null);
    }

    public HateoasKeysetPageableHandlerMethodArgumentResolver(HateoasSortHandlerMethodArgumentResolver sortResolver, ConversionService conversionService, ObjectMapper mapper) {
        super(getDefaultedSortResolver(sortResolver), conversionService, mapper);
    }

    @Override
    public boolean isOneIndexedParameters() {
        return super.isOneIndexedParameters();
    }

    @Override
    public String getParameterNameToUse(String source, MethodParameter parameter) {
        return super.getParameterNameToUse(source, parameter);
    }

    @Override
    public String getPreviousPageParameterName() {
        return super.getPreviousPageParameterName();
    }

    @Override
    public String getLowestParameterName() {
        return super.getLowestParameterName();
    }

    @Override
    public String getHighestParameterName() {
        return super.getHighestParameterName();
    }

    @Override
    public void enhance(UriComponentsBuilder builder, MethodParameter parameter, Object value) {
        Assert.notNull(builder, "UriComponentsBuilder must not be null!");

        if (!(value instanceof Pageable)) {
            return;
        }

        Pageable pageable = (Pageable) value;

        if (pageable.isUnpaged()) {
            return;
        }

        String pagePropertyName = getParameterNameToUse(getPageParameterName(), parameter);
        String sizePropertyName = getParameterNameToUse(getSizeParameterName(), parameter);

        int pageNumber = pageable.getPageNumber();

        // TODO: handle offset etc.
        builder.replaceQueryParam(pagePropertyName, isOneIndexedParameters() ? pageNumber + 1 : pageNumber);
        builder.replaceQueryParam(sizePropertyName, Math.min(pageable.getPageSize(), getMaxPageSize()));

        ((HateoasSortHandlerMethodArgumentResolver) getSortResolver()).enhance(builder, parameter, pageable.getSort());
    }

    private static HateoasSortHandlerMethodArgumentResolver getDefaultedSortResolver(HateoasSortHandlerMethodArgumentResolver sortResolver) {
        return sortResolver == null ? DEFAULT_SORT_RESOLVER : sortResolver;
    }
}
