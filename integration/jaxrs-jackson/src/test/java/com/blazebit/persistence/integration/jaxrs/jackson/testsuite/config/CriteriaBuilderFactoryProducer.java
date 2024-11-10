/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ApplicationScoped
public class CriteriaBuilderFactoryProducer {
    // inject your entity manager factory
    @Inject
    private EntityManagerFactory entityManagerFactory;

    private volatile CriteriaBuilderFactory criteriaBuilderFactory;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @PostConstruct
    public void createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        // do some configuration
        this.criteriaBuilderFactory = config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory produceCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }
}
