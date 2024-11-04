/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@jakarta.ejb.Singleton
@jakarta.ejb.Startup
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

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }

}
