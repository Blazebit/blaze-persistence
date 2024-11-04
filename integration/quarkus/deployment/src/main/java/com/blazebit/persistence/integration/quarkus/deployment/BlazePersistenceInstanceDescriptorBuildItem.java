/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.integration.quarkus.runtime.BlazePersistenceInstanceConfiguration;
import io.quarkus.builder.item.MultiBuildItem;

import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
public final class BlazePersistenceInstanceDescriptorBuildItem extends MultiBuildItem {
    private final String blazePersistenceInstanceName;
    private final BlazePersistenceInstanceConfiguration blazePersistenceConfig;
    private final Set<String> entityViewClasses;
    private final Set<String> entityViewListenerClasses;

    public BlazePersistenceInstanceDescriptorBuildItem(String blazePersistenceInstanceName, BlazePersistenceInstanceConfiguration blazePersistenceConfig, Set<String> entityViewClasses, Set<String> entityViewListenerClasses) {
        this.blazePersistenceInstanceName = blazePersistenceInstanceName;
        this.blazePersistenceConfig = blazePersistenceConfig;
        this.entityViewClasses = entityViewClasses;
        this.entityViewListenerClasses = entityViewListenerClasses;
    }

    public String getBlazePersistenceInstanceName() {
        return blazePersistenceInstanceName;
    }

    public BlazePersistenceInstanceConfiguration getBlazePersistenceConfig() {
        return blazePersistenceConfig;
    }

    public Set<String> getEntityViewClasses() {
        return entityViewClasses;
    }

    public Set<String> getEntityViewListenerClasses() {
        return entityViewListenerClasses;
    }
}
