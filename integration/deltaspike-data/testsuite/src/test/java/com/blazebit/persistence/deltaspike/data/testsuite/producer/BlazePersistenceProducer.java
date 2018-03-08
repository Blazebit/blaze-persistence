/*
 * Copyright 2014 - 2018 Blazebit.
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
