/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.producer;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.deltaspike.data.testsuite.qualifier.Restricted;
import com.blazebit.persistence.deltaspike.data.testsuite.view.ChildView;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import com.blazebit.persistence.deltaspike.data.testsuite.view.RestrictedPersonView;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
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

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(emf);
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        configuration.addEntityView(PersonView.class);
        configuration.addEntityView(ChildView.class);
        return configuration.createEntityViewManager(criteriaBuilderFactory);
    }

    @Produces
    @Restricted
    @ApplicationScoped
    public EntityViewManager createRestrictedEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        configuration.addEntityView(RestrictedPersonView.class);
        return configuration.createEntityViewManager(criteriaBuilderFactory);
    }

}
