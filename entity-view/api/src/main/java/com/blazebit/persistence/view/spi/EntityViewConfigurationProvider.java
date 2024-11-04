/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

/**
 * Interface implemented by the entity view provider.
 *
 * It is invoked to create entity view configurations.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface EntityViewConfigurationProvider {

    /**
     * Creates a new {@linkplain EntityViewConfiguration} and returns it.
     *
     * @return A new entity view configuration
     */
    public EntityViewConfiguration createConfiguration();

}
