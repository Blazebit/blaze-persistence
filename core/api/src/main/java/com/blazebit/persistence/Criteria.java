/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.CriteriaBuilderConfigurationProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Bootstrap class that is used to obtain a {@linkplain CriteriaBuilder} instance.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class Criteria {

    private static volatile CriteriaBuilderConfigurationProvider cachedDefaultProvider;

    private Criteria() {
    }

    /**
     * Returns the first {@linkplain CriteriaBuilderConfigurationProvider} that is found.
     *
     * @return The first {@linkplain CriteriaBuilderConfigurationProvider} that is found
     */
    public static CriteriaBuilderConfigurationProvider getDefaultProvider() {
        CriteriaBuilderConfigurationProvider cachedDefaultProvider = Criteria.cachedDefaultProvider;
        if (cachedDefaultProvider == null) {
            ServiceLoader<CriteriaBuilderConfigurationProvider> serviceLoader = ServiceLoader.load(CriteriaBuilderConfigurationProvider.class);
            Iterator<CriteriaBuilderConfigurationProvider> iterator = serviceLoader.iterator();

            if (iterator.hasNext()) {
                return Criteria.cachedDefaultProvider = iterator.next();
            }

            throw new IllegalStateException("No CriteriaBuilderConfigurationProvider found on the class path. Please check if a valid implementation is on the class path.");
        }
        return cachedDefaultProvider;
    }

    /**
     * Uses the default {@linkplain CriteriaBuilderConfigurationProvider} and invokes
     * {@link CriteriaBuilderConfigurationProvider#createConfiguration() }.
     *
     * @return A new criteria builder configuration
     */
    public static CriteriaBuilderConfiguration getDefault() {
        return getDefaultProvider().createConfiguration(DefaultPackageOpener.INSTANCE);
    }
}
