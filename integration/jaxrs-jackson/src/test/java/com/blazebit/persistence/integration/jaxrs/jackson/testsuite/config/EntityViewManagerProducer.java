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

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * @author Christian Beikov
 * @since 1.5.0
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
