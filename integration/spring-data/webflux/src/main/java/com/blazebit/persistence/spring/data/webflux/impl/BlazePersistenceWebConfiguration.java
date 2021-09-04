/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.spring.data.webflux.impl;

import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewAwareJackson2JsonDecoder;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewIdAwareWebFilter;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewIdValueAccessorImpl;
import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewTypedDecoderHttpMessageReader;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.WebFilter;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Configuration
public class BlazePersistenceWebConfiguration implements WebFluxConfigurer {

    private final EntityViewManager entityViewManager;
    private final ObjectProvider<ConversionService> conversionServiceFactory;
    private final ObjectMapper objectMapper;

    public BlazePersistenceWebConfiguration(
            EntityViewManager entityViewManager,
            ObjectProvider<ConversionService> conversionServiceFactory,
            @Autowired(required = false) ObjectMapper objectMapper) {
        this.entityViewManager = entityViewManager;
        this.conversionServiceFactory = conversionServiceFactory;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper.copy();
    }

    protected ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Bean
    public SortHandlerMethodArgumentResolver sortResolver() {
        return new SortHandlerMethodArgumentResolver();
    }

    @Bean
    public KeysetPageableHandlerMethodArgumentResolver keysetPageableResolver() {
        return new KeysetPageableHandlerMethodArgumentResolver(sortResolver(), objectMapper());
    }

    @Bean
    public WebFilter entityViewIdAwareWebFilter(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new EntityViewIdAwareWebFilter(requestMappingHandlerMapping, entityViewManager);
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(sortResolver());
        configurer.addCustomResolver(keysetPageableResolver());
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().reader(new EntityViewTypedDecoderHttpMessageReader<>(new EntityViewAwareJackson2JsonDecoder(entityViewManager, (EntityViewIdValueAccessorImpl) idAttributeAccessor())));
    }

    @Bean
    public EntityViewIdValueAccessor idAttributeAccessor() {
        return new EntityViewIdValueAccessorImpl(conversionServiceFactory.getObject());
    }
}