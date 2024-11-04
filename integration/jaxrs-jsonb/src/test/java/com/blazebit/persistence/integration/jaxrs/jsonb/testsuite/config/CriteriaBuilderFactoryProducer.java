/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.6.4
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
