/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;

@jakarta.ejb.Singleton
@jakarta.ejb.Startup
public class EntityViewManagerProducer {

    // inject the configuration provided by the cdi integration
    @Inject
    private EntityViewConfiguration config;

    // inject the criteria builder factory which will be used along with the entity view manager
    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    private EntityViewManager evm;

    @PostConstruct
    public void init() {
        // do some configuration
        evm = config.createEntityViewManager(criteriaBuilderFactory);
    }

    @Produces
    @ApplicationScoped
    public EntityViewManager createEntityViewManager() {
        return evm;
    }
}