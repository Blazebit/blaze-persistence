/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

    private static volatile EntityViewConfigurationProvider cachedDefaultProvider;

    private EntityViews() {
    }

    /**
     * Returns the first {@linkplain EntityViewConfigurationProvider} that is found.
     *
     * @return The first {@linkplain EntityViewConfigurationProvider} that is found
     */
    public static EntityViewConfigurationProvider getDefaultProvider() {
        EntityViewConfigurationProvider defaultEntityViewConfigurationProvider = EntityViews.cachedDefaultProvider;
        if (defaultEntityViewConfigurationProvider == null) {
            ServiceLoader<EntityViewConfigurationProvider> serviceLoader = ServiceLoader.load(EntityViewConfigurationProvider.class);
            Iterator<EntityViewConfigurationProvider> iterator = serviceLoader.iterator();

            if (iterator.hasNext()) {
                return EntityViews.cachedDefaultProvider = iterator.next();
            }

            throw new IllegalStateException(
                    "No EntityViewConfigurationProvider found on the class path. Please check if a valid implementation is on the class path.");
        }
        return defaultEntityViewConfigurationProvider;
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
