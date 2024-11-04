/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * Interface implemented by the criteria provider.
 *
 * It is used to integrate some features with the persistence provider.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface EntityManagerFactoryIntegrator {
    
    /**
     * Returns the name of dbms for which the given entity manager factory operates.
     * 
     * @param entityManagerFactory Then entity manager factory for which to retrieve the dbms from
     * @return The name of the dbms
     * @since 1.1.0
     */
    public String getDbms(EntityManagerFactory entityManagerFactory);

    /**
     * Returns the jpa provider factory for the jpa provider of the given entity manager factory.
     *
     * @param entityManagerFactory Then entity manager factory for which to retrieve jpa provider factory for
     * @return The jpa provider factory
     * @since 1.2.0
     */
    public JpaProviderFactory getJpaProviderFactory(EntityManagerFactory entityManagerFactory);

    /**
     * Registers the given functions under the given names on the given entity manager factory.
     * The dbmsFunctions map the function name to a map of dbms specific functions.
     * The dbms specific functions map uses a dbms identifier as key.
     *
     * @param entityManagerFactory The entity manager factory which should be enriched
     * @param dbmsFunctions The functions for various dbms
     * @return The enriched entity manager
     */
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions);

    /**
     * Returns all registered functions as map with the function name as key and a {@link JpqlFunction}.
     *
     * @param entityManagerFactory The entity manager factory which should be queried
     * @return The registered functions
     */
    public Map<String, JpqlFunction> getRegisteredFunctions(EntityManagerFactory entityManagerFactory);
}
