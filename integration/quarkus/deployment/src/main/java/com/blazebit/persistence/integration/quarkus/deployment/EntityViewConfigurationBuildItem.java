/*
 * Copyright 2014 - 2021 Blazebit.
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
