/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.spi;

/**
 * Simple service provider that is instantiated via {@link java.util.ServiceLoader} means.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface HibernateVersionProvider {

    /**
     * Returns the overridden version of Hibernate that should be used.
     *
     * @return The version
     */
    public String getVersion();

}
