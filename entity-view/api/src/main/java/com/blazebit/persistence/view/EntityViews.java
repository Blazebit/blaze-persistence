/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewConfigurationProvider;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Bootstrap class that is used to obtain a {@linkplain EntityViewConfiguration} instance within Java SE environments.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViews {

    private EntityViews() {
    }

    /**
     * Returns the first {@linkplain EntityViewConfigurationProvider} that is found.
     *
     * @return The first {@linkplain EntityViewConfigurationProvider} that is found
     */
    public static EntityViewConfigurationProvider getDefaultProvider() {
        ServiceLoader<EntityViewConfigurationProvider> serviceLoader = ServiceLoader.load(EntityViewConfigurationProvider.class);
        Iterator<EntityViewConfigurationProvider> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException(
            "No EntityViewConfigurationProvider found on the class path. Please check if a valid implementation is on the class path.");
    }

    /**
     * Uses the default {@linkplain EntityViewConfigurationProvider} and invokes {@link EntityViewConfigurationProvider#createConfiguration() }.
     *
     * @return A new entity view configuration
     */
    public static EntityViewConfiguration createDefaultConfiguration() {
        return getDefaultProvider()
            .createConfiguration();
    }
}
