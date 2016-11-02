/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractJpaPersistenceTest {

    protected EntityManagerFactory emf;
    protected EntityManager em;
    protected CriteriaBuilderFactory cbf;

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(AbstractJpaPersistenceTest.class.getResourceAsStream(
                    "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void init() {
        emf = createEntityManagerFactory("TestsuiteBase", createProperties("drop-and-create"));
        em = emf.createEntityManager();

        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config = configure(config);
        cbf = config.createCriteriaBuilderFactory(emf);
    }

    @After
    public void destruct() {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        // NOTE: We need to close the entity manager or else we could run into a deadlock on some dbms platforms
        // I am looking at you MySQL..
        em.close();
        factory.close();

        // Drop the tables again with the same persistence unit
        factory = createEntityManagerFactory("TestsuiteBase", createProperties("drop"));
        factory.close();
    }
    
    private Properties createProperties(String dbAction) {
        Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.url", System.getProperty("jdbc.url"));
        properties.put("javax.persistence.jdbc.user", System.getProperty("jdbc.user", ""));
        properties.put("javax.persistence.jdbc.password", System.getProperty("jdbc.password", ""));
        properties.put("javax.persistence.jdbc.driver", System.getProperty("jdbc.driver"));
        properties.put("javax.persistence.sharedCache.mode", "NONE");
        properties.put("javax.persistence.schema-generation.database.action", dbAction);
        properties = applyProperties(properties);
        return properties;
    }

    protected abstract Class<?>[] getEntityClasses();
    
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        return config;
    }

    protected Properties applyProperties(Properties properties) {
        return properties;
    }

    private EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map<Object, Object> properties) {
        MutablePersistenceUnitInfo persistenceUnitInfo = new MutablePersistenceUnitInfo();
        persistenceUnitInfo.setPersistenceUnitName(persistenceUnitName);
        persistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        persistenceUnitInfo.setExcludeUnlistedClasses(true);

        try {
            URL url = AbstractJpaPersistenceTest.class.getClassLoader()
                .getResource("");
            persistenceUnitInfo.setPersistenceUnitRootUrl(url);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        for (Class<?> clazz : getEntityClasses()) {
            persistenceUnitInfo.addManagedClassName(clazz.getName());
        }

        return createEntityManagerFactory(persistenceUnitInfo, properties);
    }

    private static EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo, Map<Object, Object> properties) {
        EntityManagerFactory factory = null;
        Map<Object, Object> props = properties;
        if (props == null) {
            props = Collections.emptyMap();
        }

        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        Map<String, Throwable> exceptions = new HashMap<String, Throwable>();
        StringBuffer foundProviders = null;

        for (PersistenceProvider provider : providers) {
            String providerName = provider.getClass()
                .getName();
            try {
                factory = provider.createContainerEntityManagerFactory(persistenceUnitInfo, props);
            } catch (Exception e) {
                // capture the exception details and give other providers a chance
                exceptions.put(providerName, e);
            }
            if (factory != null) {
                // we're done
                return factory;
            } else {
                // update the list of providers we have tried
                if (foundProviders == null) {
                    foundProviders = new StringBuffer(providerName);
                } else {
                    foundProviders.append(", ");
                    foundProviders.append(providerName);
                }
            }
        }

        // make sure our providers list is initialized for the exceptions below
        if (foundProviders == null) {
            foundProviders = new StringBuffer("NONE");
        }

        if (exceptions.isEmpty()) {
            // throw an exception with the PU name and providers we tried
            throw new PersistenceException("No persistence providers available for \"" + persistenceUnitInfo
                .getPersistenceUnitName() + "\" after trying the following discovered implementations: " + foundProviders);
        } else {
            // we encountered one or more exceptions, so format and throw as a single exception
            throw createPersistenceException(
                "Explicit persistence provider error(s) occurred for \"" + persistenceUnitInfo.getPersistenceUnitName()
                + "\" after trying the following discovered implementations: " + foundProviders,
                exceptions);
        }
    }

    private static PersistenceException createPersistenceException(String msg, Map<String, Throwable> failures) {
        String newline = System.getProperty("line.separator");
        StringWriter strWriter = new StringWriter();
        strWriter.append(msg);
        if (failures.size() <= 1) {
            // we caught an exception, so include it as the cause
            Throwable t = null;
            for (String providerName : failures.keySet()) {
                t = failures.get(providerName);
                strWriter.append(" from provider: ");
                strWriter.append(providerName);
                break;
            }
            return new PersistenceException(strWriter.toString(), t);
        } else {
            // we caught multiple exceptions, so format them into the message string and don't set a cause
            strWriter.append(" with the following failures:");
            strWriter.append(newline);
            for (String providerName : failures.keySet()) {
                strWriter.append(providerName);
                strWriter.append(" returned: ");
                failures.get(providerName)
                    .printStackTrace(new PrintWriter(strWriter));
            }
            strWriter.append(newline);
            return new PersistenceException(strWriter.toString());
        }
    }
}
