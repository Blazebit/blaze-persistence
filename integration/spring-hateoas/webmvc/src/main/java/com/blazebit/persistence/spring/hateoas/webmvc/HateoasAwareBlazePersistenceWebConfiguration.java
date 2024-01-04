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

import com.blazebit.persistence.spring.data.webmvc.KeysetPageableArgumentResolver;
import com.blazebit.persistence.spring.data.webmvc.impl.BlazePersistenceWebConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@Configuration
public class HateoasAwareBlazePersistenceWebConfiguration extends BlazePersistenceWebConfiguration {

    private final List<? extends UriComponentsContributor> uriComponentsContributors;

    public HateoasAwareBlazePersistenceWebConfiguration(
            EntityViewManager entityViewManager,
            @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService,
            @Lazy List<UriComponentsContributor> uriComponentsContributors,
            @Autowired(required = false) ObjectMapper objectMapper) {
        super(entityViewManager, conversionService, objectMapper);
        this.uriComponentsContributors = uriComponentsContributors;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        boolean hasPageableResolver = false;
        for (int i = 0; i < argumentResolvers.size(); i++) {
            HandlerMethodArgumentResolver argumentResolver = argumentResolvers.get(i);
            if (argumentResolver.getClass() == KeysetPageableArgumentResolver.class) {
                argumentResolvers.remove(i);
                break;
            } else if (argumentResolver instanceof HateoasKeysetPageableHandlerMethodArgumentResolver) {
                hasPageableResolver = true;
            }
        }
        if (!hasPageableResolver) {
            // Add it to the beginning so it has precedence over the builtin
            argumentResolvers.add(0, blazeWebmvcKeysetPageableResolver());
        }
        argumentResolvers.add(keysetPagedResourcesAssemblerArgumentResolver());
    }

    @Bean
    @Override
    public HateoasKeysetPageableHandlerMethodArgumentResolver blazeWebmvcKeysetPageableResolver() {
        return new HateoasKeysetPageableHandlerMethodArgumentResolver(blazeWebmvcKeysetSortResolver(), conversionService.getObject(), objectMapper());
    }

    @Bean
    @Override
    public HateoasSortHandlerMethodArgumentResolver blazeWebmvcKeysetSortResolver() {
        return new HateoasSortHandlerMethodArgumentResolver();
    }

    @Bean
    public KeysetAwarePagedResourcesAssembler<?> keysetPagedResourcesAssembler() {
        return new KeysetAwarePagedResourcesAssembler<Object>(blazeWebmvcKeysetPageableResolver(), null, objectMapper());
    }

    @Bean
    public KeysetAwarePagedResourcesAssemblerArgumentResolver keysetPagedResourcesAssemblerArgumentResolver() {
        WebMvcLinkBuilderFactory linkBuilderFactory = new WebMvcLinkBuilderFactory();
        linkBuilderFactory.setUriComponentsContributors(uriComponentsContributors);
        return new KeysetAwarePagedResourcesAssemblerArgumentResolver(blazeWebmvcKeysetPageableResolver(), linkBuilderFactory, objectMapper());
    }

}
