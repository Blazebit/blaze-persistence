/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class EntityViewManagerProducer {

    // inject the configuration provided by the cdi integration
    @Inject
    private EntityViewConfiguration config;

    // inject the criteria builder factory which will be used along with the entity view manager
    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    private volatile EntityViewManager evm;

    @PostConstruct
    public void init() {
        // do some configuration
        evm = config.createEntityViewManager(criteriaBuilderFactory);
    }

    void onStart(@Observes @Initialized(ApplicationScoped.class) Object ev) {
        // Empty observer to trigger eager bean initialization
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager createEntityViewManager() {
        return evm;
    }
}