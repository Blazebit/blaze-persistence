/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.producer;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class BlazePersistenceProducer {

    @Inject
    private EntityManagerFactory emf;

    @Inject
    private EntityViewConfiguration entityViewConfiguration;

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(emf);
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory) {
        return entityViewConfiguration.createEntityViewManager(criteriaBuilderFactory);
    }

}
