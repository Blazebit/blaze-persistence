/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Configuration
@EntityScan("com.blazebit.persistence.examples.itsm")
@EnableEntityViews("com.blazebit.persistence.examples.itsm")
@EnableBlazeRepositories(basePackages = "com.blazebit.persistence.examples.itsm")
public class BlazeConfiguration {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(this.entityManagerFactory);
    }

    @Bean
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory cbf,
            EntityViewConfiguration entityViewConfiguration) {
        return entityViewConfiguration.createEntityViewManager(cbf);
    }

}
