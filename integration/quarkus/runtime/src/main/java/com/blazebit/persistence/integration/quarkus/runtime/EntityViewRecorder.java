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

package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Recorder
public class EntityViewRecorder {

    private List<Class<?>> entitiesViews = new ArrayList<>();

    public void addEntityView(Class<?> entityView) {
        this.entitiesViews.add(entityView);
    }

    public BeanContainerListener setEntityViewConfiguration() {
        return beanContainer -> {
            EntityViewConfigurationHolder configurationHolder = beanContainer.instance(EntityViewConfigurationHolder.class);
            EntityViewConfiguration entityViewConfiguration = EntityViews.createDefaultConfiguration();
            for (Class<?> entityView : entitiesViews) {
                entityViewConfiguration.addEntityView(entityView);
            }
            configurationHolder.setEntityViewConfiguration(entityViewConfiguration);
        };
    }
}
