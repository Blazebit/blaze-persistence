/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import jakarta.persistence.EntityManager;

/**
 * A service provider factory to create {@link JpaProvider} instances.
 * Instances are created via {@link java.util.ServiceLoader} means.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface JpaProviderFactory {

    /**
     * Create a {@link JpaProvider} for the given entity manager.
     *
     * @param em The optional entity manager to use for creating the provider
     * @return The jpa provider instance
     */
    public JpaProvider createJpaProvider(EntityManager em);
}
