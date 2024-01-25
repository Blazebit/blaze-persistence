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

package com.blazebit.persistence.spring.data.webmvc.impl;

import com.blazebit.persistence.integration.jackson.EntityViewIdValueAccessor;
import com.blazebit.persistence.spring.data.webmvc.KeysetPageableArgumentResolver;
import com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewAwareMappingJackson2HttpMessageConverter;
import com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdHandlerInterceptor;
import com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdValueHolder;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Configuration
public class BlazePersistenceWebConfiguration implements WebMvcConfigurer {

    protected final ObjectFactory<ConversionService> conversionService;
    protected final ObjectMapper objectMapper;
    private final EntityViewManager entityViewManager;

    public BlazePersistenceWebConfiguration(EntityViewManager entityViewManager,
            @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService,
            @Autowired(required = false) ObjectMapper objectMapper) {
        this.entityViewManager = entityViewManager;
        this.conversionService = conversionService;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper.copy();
    }

    protected ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Bean
    public KeysetPageableArgumentResolver blazeWebmvcKeysetPageableResolver() {
        return new KeysetPageableHandlerMethodArgumentResolver(blazeWebmvcKeysetSortResolver(), conversionService.getObject(), objectMapper());
    }

    @Bean
    public SortHandlerMethodArgumentResolver blazeWebmvcKeysetSortResolver() {
        return new SortHandlerMethodArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        for (int i = 0; i < argumentResolvers.size(); i++) {
            if (argumentResolvers.get(i) instanceof KeysetPageableArgumentResolver) {
                return;
            }
        }

        // Add it to the beginning so it has precedence over the builtin
        argumentResolvers.add(0, blazeWebmvcKeysetPageableResolver());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Add it to the beginning, so it has precedence over the builtin
        converters.add(0, new EntityViewAwareMappingJackson2HttpMessageConverter(entityViewManager, blazeWebmvcIdAttributeAccessor(), objectMapper()));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new EntityViewIdHandlerInterceptor(entityViewManager, (EntityViewIdValueHolder) blazeWebmvcIdAttributeAccessor()));
    }

    @Bean
    public EntityViewIdValueAccessor blazeWebmvcIdAttributeAccessor() {
        return new EntityViewIdValueHolder(conversionService.getObject());
    }
}
