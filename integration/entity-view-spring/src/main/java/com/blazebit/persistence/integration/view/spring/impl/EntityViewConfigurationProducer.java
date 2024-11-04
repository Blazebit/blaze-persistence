/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Configuration
public class EntityViewConfigurationProducer {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
    private PlatformTransactionManager tm;

    public EntityViewConfigurationProducer(Set<Class<?>> entityViewClasses, Set<Class<?>> entityViewListenerClasses) {
        for (Class<?> entityViewClass : entityViewClasses) {
            configuration.addEntityView(entityViewClass);
        }
        for (Class<?> entityViewListenerClass : entityViewListenerClasses) {
            configuration.addEntityViewListener(entityViewListenerClass);
        }
    }

    @Autowired(required = false)
    public void setTm(PlatformTransactionManager tm) {
        this.tm = tm;
    }

    @Bean
    public EntityViewConfiguration getEntityViewConfiguration() {
        if (tm != null) {
            configuration.setTransactionSupport(new SpringTransactionSupport(tm));
        }
        return configuration;
    }


}
