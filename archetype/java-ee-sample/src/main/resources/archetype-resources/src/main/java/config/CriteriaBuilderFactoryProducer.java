/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@javax.ejb.Singleton
@javax.ejb.Startup
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
