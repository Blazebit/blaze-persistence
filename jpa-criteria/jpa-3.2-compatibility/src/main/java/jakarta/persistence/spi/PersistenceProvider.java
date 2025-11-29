/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence.spi;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceConfiguration;
import jakarta.persistence.PersistenceException;
import java.util.Map;

/**
 * Interface implemented by the persistence provider.
 *
 * <p> It is invoked by the container in Jakarta EE environments and
 * by the {@link Persistence} class in Java SE environments to create
 * an {@link EntityManagerFactory} and/or to cause schema generation
 * to occur.
 *
 * @since 1.0
 */
public interface PersistenceProvider {

    /**
     * Called by {@link Persistence} class when an
     * {@link EntityManagerFactory} is to be created.
     *
     * @param emName  the name of the persistence unit
     * @param map  a Map of properties for use by the 
     * persistence provider. These properties may be used to
     * override the values of the corresponding elements in 
     * the {@code persistence.xml} file or specify values for
     * properties not specified in the {@code persistence.xml}
     * (and may be null if no properties are specified).
     * @return EntityManagerFactory for the persistence unit, 
     * or null if the provider is not the right provider
     *
     * @see Persistence#createEntityManagerFactory(String, Map)
     */
    EntityManagerFactory createEntityManagerFactory(String emName, Map<?, ?> map);

    /**
     * Called by {@link Persistence} class when an
     * {@link EntityManagerFactory} is to be created.
     *
     * @param configuration  the configuration of the persistence unit
     * @return EntityManagerFactory for the persistence unit,
     * or null if the provider is not the right provider
     * @throws IllegalStateException if required configuration is missing
     *
     * @see Persistence#createEntityManagerFactory(PersistenceConfiguration)
     *
     * @since 3.2
     */
    EntityManagerFactory createEntityManagerFactory(PersistenceConfiguration configuration);

    /**
     * Called by the container when an {@link EntityManagerFactory}
     * is to be created. 
     *
     * @param info  metadata for use by the persistence provider
     * @param map  a Map of integration-level properties for use 
     * by the persistence provider (may be null if no properties
     * are specified). These properties may include properties to
     * control schema generation. If a Bean Validation provider is
     * present in the classpath, the container must pass the
     * {@code ValidatorFactory} instance in the map with the key
     * {@code "jakarta.persistence.validation.factory"}. If the
     * containing archive is a bean archive, the container must
     * pass the {@code BeanManager} instance in the map with the
     * key {@code "jakarta.persistence.bean.manager"}.
     * @return {@link EntityManagerFactory} for the persistence unit
     * specified by the metadata
     */
    EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map<?, ?> map);


    /**
     * Create database schemas and/or tables and/or create DDL
     * scripts as determined by the supplied properties.
     * <p>
     * Called by the container when schema generation is to
     * occur as a separate phase from creation of the entity
     * manager factory.
     * <p>
     * @param info metadata for use by the persistence provider
     * @param map properties for schema generation;  these may
     *            also include provider-specific properties
     * @throws PersistenceException if insufficient or inconsistent
     *         configuration information is provided of if schema
     *         generation otherwise fails
     *
     * @since 2.1
     */
    void generateSchema(PersistenceUnitInfo info, Map<?, ?> map);

    /**
     * Create database schemas and/or tables and/or create DDL
     * scripts as determined by the supplied properties.
     * <p>
     * Called by the {@link Persistence} class when schema generation
     * is to occur as a separate phase from creation of the entity
     * manager factory.
     * <p>
     * @param persistenceUnitName the name of the persistence unit
     * @param map properties for schema generation;  these may
     *            also contain provider-specific properties.  The
     *            value of these properties override any values that
     *            may have been configured elsewhere.
     * @return true  if schema was generated, otherwise false
     * @throws PersistenceException if insufficient or inconsistent
     *         configuration information is provided or if schema
     *         generation otherwise fails
     *
     * @since 2.1
     */
    boolean generateSchema(String persistenceUnitName, Map<?, ?> map);

    /**
     * Return the utility interface implemented by the persistence
     * provider.
     * @return an instance of {@link ProviderUtil}
     *
     * @since 2.0
     */
    ProviderUtil getProviderUtil();
}

