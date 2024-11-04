/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Interface implemented by the criteria provider.
 *
 * Implementations are instantiated via {@link java.util.ServiceLoader}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CriteriaBuilderConfigurationProvider {

    /**
     * Creates and returns a new criteria builder configuration.
     *
     * @return A new criteria builder configuration
     */
    public CriteriaBuilderConfiguration createConfiguration();

    /**
     * Creates and returns a new criteria builder configuration.
     *
     * @param packageOpener The package opener to use to obtain access to user classes
     * @return A new criteria builder configuration
     * @since 1.2.0
     */
    public CriteriaBuilderConfiguration createConfiguration(PackageOpener packageOpener);
}
