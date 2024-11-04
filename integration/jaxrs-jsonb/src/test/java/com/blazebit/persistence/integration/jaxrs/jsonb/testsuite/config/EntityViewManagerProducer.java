/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@ApplicationScoped
public class EntityViewManagerProducer {

    // inject the configuration provided by the cdi integration
    @Inject
    private EntityViewConfiguration evmConfig;

    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    private EntityViewManager evm;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @PostConstruct
    public void createEntityViewManager() {
        evmConfig.registerTypeConverter(String.class, UUID.class, new TypeConverter<String, UUID>() {
            @Override
            public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
                return String.class;
            }

            @Override
            public UUID convertToViewType(String object) {
                return object == null ? null : UUID.fromString(object);
            }

            @Override
            public String convertToUnderlyingType(UUID object) {
                return object == null ? null : object.toString();
            }
        });
        evm = evmConfig.createEntityViewManager(criteriaBuilderFactory);
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager produceEntityViewManager() {
        return evm;
    }
}
