/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ApplicationScoped
public class CriteriaBuilderConfigurationHolder {

    private volatile CriteriaBuilderConfiguration criteriaBuilderConfiguration;

    @Produces
    public CriteriaBuilderConfiguration getCriteriaBuilderConfiguration() {
        return criteriaBuilderConfiguration;
    }

    public void setCriteriaBuilderConfiguration(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        this.criteriaBuilderConfiguration = criteriaBuilderConfiguration;
    }
}
