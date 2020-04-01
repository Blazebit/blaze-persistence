/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.jaxrs.testsuite.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
        evm = evmConfig.createEntityViewManager(criteriaBuilderFactory);
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager produceEntityViewManager() {
        return evm;
    }
}
