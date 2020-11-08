/*
 * Copyright 2014 - 2020 Blazebit.
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
