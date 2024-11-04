/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import io.quarkus.builder.item.SimpleBuildItem;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public final class EntityViewConfigurationBuildItem extends SimpleBuildItem {
    private final EntityViewConfiguration entityViewConfiguration;

    public EntityViewConfigurationBuildItem(EntityViewConfiguration entityViewConfiguration) {
        this.entityViewConfiguration = entityViewConfiguration;
    }

    public EntityViewConfiguration getEntityViewConfiguration() {
        return entityViewConfiguration;
    }
}
