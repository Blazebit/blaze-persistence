/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.quarkus.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public final class EntityViewsBuildItem extends SimpleBuildItem {
    private final Set<String> entityViewClassNames = new HashSet<>();

    void addEntityViewClass(final String className) {
        entityViewClassNames.add(className);
    }

    public Set<String> getEntityViewClassNames() {
        return entityViewClassNames;
    }
}
