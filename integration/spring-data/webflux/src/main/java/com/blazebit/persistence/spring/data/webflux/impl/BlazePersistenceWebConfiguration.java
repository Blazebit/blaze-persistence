/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.spring.data.webflux.impl.json.EntityViewAwareJackson2JsonDecoder;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Configuration
public class BlazePersistenceWebConfiguration implements WebFluxConfigurer {

    private final EntityViewManager entityViewManager;

    @Autowired
    public BlazePersistenceWebConfiguration(EntityViewManager entityViewManager) {
        this.entityViewManager = entityViewManager;
    }

    @Bean
    public SortHandlerMethodArgumentResolver sortResolver() {
        return new SortHandlerMethodArgumentResolver();
    }

    @Bean
    public KeysetPageableHandlerMethodArgumentResolver keysetPageableResolver() {
        return new KeysetPageableHandlerMethodArgumentResolver(sortResolver());
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(sortResolver());
        configurer.addCustomResolver(keysetPageableResolver());
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().jackson2JsonDecoder(new EntityViewAwareJackson2JsonDecoder(entityViewManager));
    }

}