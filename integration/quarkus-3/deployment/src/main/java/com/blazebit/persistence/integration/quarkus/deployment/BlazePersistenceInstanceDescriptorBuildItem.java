/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment;

import io.quarkus.builder.item.MultiBuildItem;

import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
public final class BlazePersistenceInstanceDescriptorBuildItem extends MultiBuildItem {
    private final String blazePersistenceInstanceName;
    private final Set<String> entityViewClasses;
    private final Set<String> entityViewListenerClasses;

    public BlazePersistenceInstanceDescriptorBuildItem(String blazePersistenceInstanceName, Set<String> entityViewClasses, Set<String> entityViewListenerClasses) {
        this.blazePersistenceInstanceName = blazePersistenceInstanceName;
        this.entityViewClasses = entityViewClasses;
        this.entityViewListenerClasses = entityViewListenerClasses;
    }

    public String getBlazePersistenceInstanceName() {
        return blazePersistenceInstanceName;
    }

    public Set<String> getEntityViewClasses() {
        return entityViewClasses;
    }

    public Set<String> getEntityViewListenerClasses() {
        return entityViewListenerClasses;
    }
}
