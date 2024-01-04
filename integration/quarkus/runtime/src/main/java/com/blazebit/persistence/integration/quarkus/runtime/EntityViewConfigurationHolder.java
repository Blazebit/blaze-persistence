/*
 * Copyright 2014 - 2024 Blazebit.
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
