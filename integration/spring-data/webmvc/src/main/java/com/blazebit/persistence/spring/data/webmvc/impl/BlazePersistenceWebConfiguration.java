/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Configuration
public class BlazePersistenceWebConfiguration extends WebMvcConfigurerAdapter {

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
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        return sortResolver;
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
        // Add it to the beginning so it has precedence over the builtin
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
