/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.config;

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
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class CriteriaBuilderFactoryProducer {

    // inject your entity manager factory
    @Inject
    private EntityManagerFactory entityManagerFactory;

    private CriteriaBuilderFactory criteriaBuilderFactory;

    @PostConstruct
    public void init() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        // do some configuration
        this.criteriaBuilderFactory = config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    void onStart(@Observes @Initialized(ApplicationScoped.class) Object ev) {
        // Empty observer to trigger eager bean initialization
    }

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }

}
