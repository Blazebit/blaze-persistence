/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ApplicationScoped
public class EntityViewConfigurationHolder {

    private volatile EntityViewConfiguration entityViewConfiguration;

    @Produces
    public EntityViewConfiguration getEntityViewConfiguration() {
        return entityViewConfiguration;
    }

    public void setEntityViewConfiguration(EntityViewConfiguration entityViewConfiguration) {
        this.entityViewConfiguration = entityViewConfiguration;
    }
}
