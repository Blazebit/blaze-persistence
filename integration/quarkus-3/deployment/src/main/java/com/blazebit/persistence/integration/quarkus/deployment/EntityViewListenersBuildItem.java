/*
 * Copyright 2014 - 2023 Blazebit.
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
package com.blazebit.persistence.integration.quarkus.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public final class EntityViewListenersBuildItem extends SimpleBuildItem {
    private final Set<String> entityViewListenerClassNames = new HashSet<>();

    void addEntityViewListenerClass(final String className) {
        entityViewListenerClassNames.add(className);
    }

    public Set<String> getEntityViewListenerClassNames() {
        return entityViewListenerClassNames;
    }
}
