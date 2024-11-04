/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Provides access to various services.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ServiceProvider {


    /**
     * Returns the service or null if none is available.
     *
     * @param serviceClass The type of the service
     * @param <T> The service type
     * @return The service or null
     */
    public <T> T getService(Class<T> serviceClass);

}
