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
//     Lukas Jungmann  - 3.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolver;
import jakarta.persistence.spi.PersistenceProviderResolverHolder;
import jakarta.persistence.spi.LoadState;

/**
 * Bootstrap class used to obtain an {@link EntityManagerFactory}
 * in Java SE environments. It may also be used to cause schema
 * generation to occur.
 * 
 * <p>The {@code Persistence} class is available in a Jakarta EE
 * container environment as well; however, support for the Java SE
 * bootstrapping APIs is not required in container environments.
 * 
 * <p>The {@code Persistence} class is used to obtain a {@link
 * PersistenceUtil PersistenceUtil} instance in both Jakarta EE
 * and Java SE environments.
 *
 * @since 1.0
 */
public class Persistence {

    /**
     * Default constructor.
     * @deprecated This class is not intended to be extended nor instantiated,
     *     it is going to be marked {@code final} when this constructor becomes hidden.
     */
    @Deprecated(since = "3.2", forRemoval = true)
    public Persistence() {
        //kept for backward compatibility with pre-3.2 versions
    }

    /**
     * Create and return an {@link EntityManagerFactory} for the named
     * persistence unit.
     * 
     * @param persistenceUnitName the name of the persistence unit
     * @return the factory that creates {@link EntityManager}s configured
     *         according to the specified persistence unit
     */
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        return createEntityManagerFactory(persistenceUnitName, null);
    }

    /**
     * Create and return an {@link EntityManagerFactory} for the named
     * persistence unit, using the given properties.
     * 
     * @param persistenceUnitName the name of the persistence unit
     * @param properties additional properties to use when creating the
     *                   factory. These properties may include properties
     *                   to control schema generation. The values of these
     *                   properties override any values that may have been
     *                   configured elsewhere.
     * @return the factory that creates {@link EntityManager}s configured
     *        according to the specified persistence unit
     */
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map<?,?> properties) {

        EntityManagerFactory emf = null;
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

        List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        for (PersistenceProvider provider : providers) {
            emf = provider.createEntityManagerFactory(persistenceUnitName, properties);
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named " + persistenceUnitName);
        }
        return emf;
    }

    /**
     * Create and return an {@link EntityManagerFactory} for the named
     * persistence unit, using the given properties.
     *
     * @param configuration configuration of the persistence unit
     * @return the factory that creates {@link EntityManager}s configured
     *         according to the specified persistence unit
     *
     * @since 3.2
     */
    public static EntityManagerFactory createEntityManagerFactory(PersistenceConfiguration configuration) {

        EntityManagerFactory emf = null;
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

        List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        for (PersistenceProvider provider : providers) {
            emf = provider.createEntityManagerFactory(configuration);
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named " + configuration.name());
        }
        return emf;
    }


    /**
     * Create database schemas and/or tables and/or create DDL scripts
     * as determined by the supplied properties.
     * <p>
     * Called when schema generation is to occur as a separate phase
     * from creation of the entity manager factory.
     * <p>
     * @param persistenceUnitName the name of the persistence unit
     * @param map properties for schema generation; these may also
     *            contain provider-specific properties. The values
     *            of these properties override any values that may
     *            have been configured elsewhere.
     * @throws PersistenceException if insufficient or inconsistent
     *         configuration information is provided or if schema
     *         generation otherwise fails.
     *
     * @since 2.1
     */
    public static void generateSchema(String persistenceUnitName, Map<?,?> map) {
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
        
        for (PersistenceProvider provider : providers) {
            if (provider.generateSchema(persistenceUnitName, map)) {
                return;
            }
        }
        
        throw new PersistenceException("No Persistence provider to generate schema named " + persistenceUnitName);
    }
    

    /**
     * Return the {@link PersistenceUtil} instance
     * @return {@link PersistenceUtil} instance
     * @since 2.0
     */
    public static PersistenceUtil getPersistenceUtil() {
       return new PersistenceUtilImpl();
    }

    
    /**
     * Implementation of the {@link PersistenceUtil} interface
     * @since 2.0
     */
    private static class PersistenceUtilImpl implements PersistenceUtil {
        public boolean isLoaded(Object entity, String attributeName) {
            PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

            List<PersistenceProvider> providers = resolver.getPersistenceProviders();

            for (PersistenceProvider provider : providers) {
                LoadState loadstate = provider.getProviderUtil().isLoadedWithoutReference(entity, attributeName);
                if(loadstate == LoadState.LOADED) {
                    return true;
                } else if (loadstate == LoadState.NOT_LOADED) {
                    return false;
                } // else continue
            }

            //None of the providers could determine the load state try isLoadedWithReference
            for (PersistenceProvider provider : providers) {
                LoadState loadstate = provider.getProviderUtil().isLoadedWithReference(entity, attributeName);
                if(loadstate == LoadState.LOADED) {
                    return true;
                } else if (loadstate == LoadState.NOT_LOADED) {
                    return false;
                } // else continue
            }

            //None of the providers could determine the load state.
            return true;
        }

        public boolean isLoaded(Object entity) {
            PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

            List<PersistenceProvider> providers = resolver.getPersistenceProviders();

            for (PersistenceProvider provider : providers) {
                LoadState loadstate = provider.getProviderUtil().isLoaded(entity);
                if(loadstate == LoadState.LOADED) {
                    return true;
                } else if (loadstate == LoadState.NOT_LOADED) {
                    return false;
                } // else continue
            }
            //None of the providers could determine the load state
            return true;
        }
    }

    /**
     * This final String is deprecated and should be removed and is only here for TCK backward compatibility
     * @since 1.0
     * @deprecated
     *
     * TODO: Either change TCK reference to PERSISTENCE_PROVIDER field to expect 
     * "jakarta.persistence.spi.PersistenceProvider" or remove PERSISTENCE_PROVIDER field and also update TCK signature 
     * tests. 
     */
    @Deprecated(since = "3.2", forRemoval = true)
    public static final String PERSISTENCE_PROVIDER = "jakarta.persistence.spi.PersistenceProvider";
    
    /**
     * This instance variable is deprecated and should be removed and is only here for TCK backward compatibility
     * @since 1.0
     * @deprecated
     */
    @Deprecated(since = "3.2", forRemoval = true)
    protected static final Set<PersistenceProvider> providers = new HashSet<PersistenceProvider>();
}
