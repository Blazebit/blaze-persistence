/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.testsuite.base.jpa;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DB2DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.H2DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.MySQLDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.OracleDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.PostgreSQLDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.SQLServerDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractJpaPersistenceTest {

    private static boolean resolvedNoop = false;
    private static boolean databaseClean = false;
    private static Class<?> lastTestClass;
    private static Set<Class<?>> databaseCleanerClasses;
    private static DatabaseCleaner databaseCleaner;
    private static HikariDataSource dataSource;
    private static final Map<Class<?>, List<String>> PLURAL_DELETES = new HashMap<>();
    private static final List<DatabaseCleaner.Factory> DATABASE_CLEANERS = Arrays.asList(
            new H2DatabaseCleaner.Factory(),
            new PostgreSQLDatabaseCleaner.Factory(),
            new DB2DatabaseCleaner.Factory(),
            new MySQLDatabaseCleaner.Factory(),
            new SQLServerDatabaseCleaner.Factory(),
            new OracleDatabaseCleaner.Factory()
    );

    protected EntityManagerFactory emf;
    protected EntityManager em;
    protected CriteriaBuilderFactory cbf;
    protected JpaProvider jpaProvider;
    protected DbmsDialect dbmsDialect;

    private boolean schemaChanged;

    static {
        System.setProperty("org.jboss.logging.provider", "jdk");
    }

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(AbstractJpaPersistenceTest.class.getResourceAsStream(
                    "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private DatabaseCleaner getLastDatabaseCleaner() {
        if (new HashSet<>(Arrays.asList(getEntityClasses())).equals(databaseCleanerClasses)) {
            return databaseCleaner;
        }
        return null;
    }

    private void setLastDatabaseCleaner(DatabaseCleaner cleaner) {
        databaseCleanerClasses = new HashSet<>(Arrays.asList(getEntityClasses()));
        databaseCleaner = cleaner;
    }

    protected void cleanDatabase() {
        // Nothing to delete if the database is "clean"
        if (databaseClean) {
            return;
        }

        boolean wasAutoCommit = false;
        Connection connection = getConnection(em);
        try {
            // Turn off auto commit if necessary
            wasAutoCommit = connection.getAutoCommit();
            if (wasAutoCommit) {
                connection.setAutoCommit(false);
            }
            // Clear the data with the cleaner
            databaseCleaner.clearData(connection);
            databaseClean = true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                ex.addSuppressed(e1);
            }

            throw new RuntimeException(ex);
        } finally {
            if (wasAutoCommit) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    protected void clearCollections(EntityManager em, Class<?>... entityClasses) {
        for (Class<?> c : entityClasses) {
            List<String> deletes = PLURAL_DELETES.get(c);
            if (deletes == null) {
                Metamodel entityMetamodel = cbf.getService(Metamodel.class);
                EntityType<?> t = entityMetamodel.entity(c);
                deletes = new ArrayList<>();
                for (PluralAttribute<?, ?, ?> pluralAttribute : t.getPluralAttributes()) {
                    JoinTable joinTable = jpaProvider.getJoinTable(t, pluralAttribute.getName());

                    if (joinTable != null) {
                        deletes.add("delete from " + joinTable.getTableName());
                    }
                }
                PLURAL_DELETES.put(c, deletes);
            }

            for (String delete : deletes) {
                em.createNativeQuery(delete).executeUpdate();
            }
        }
    }

    private void clearSchema() {
        clearSchema(em, databaseCleaner);
    }

    public void clearSchema(EntityManager em, DatabaseCleaner databaseCleaner) {
        boolean wasAutoCommit = false;
        Connection connection = getConnection(em);
        try {
            // Turn off auto commit if necessary
            wasAutoCommit = connection.getAutoCommit();
            if (wasAutoCommit) {
                connection.setAutoCommit(false);
            }
            // Clear the data with the cleaner
            databaseCleaner.clearSchema(connection);
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                ex.addSuppressed(e1);
            }

            throw new RuntimeException(ex);
        } finally {
            if (wasAutoCommit) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }


    public DatabaseCleaner getDatabaseCleanerOrDefault(EntityManager em) {
        DatabaseCleaner databaseCleaner = getDatabaseCleaner(em);
        if (databaseCleaner == null) {
            return getDefaultDatabaseCleaner();
        }
        return databaseCleaner;
    }

    public DatabaseCleaner getDefaultDatabaseCleaner() {
        return new DatabaseCleaner() {
            @Override
            public boolean isApplicable(Connection connection) {
                return true;
            }

            @Override
            public boolean supportsClearSchema() {
                return false;
            }

            @Override
            public void clearSchema(Connection connection) {
            }

            @Override
            public void addIgnoredTable(String tableName) {
            }

            @Override
            public void clearData(Connection connection) {
                recreateOrClearSchema();
            }
        };
    }

    public DatabaseCleaner getDatabaseCleaner(EntityManager em) {
        // Find an applicable cleaner
        Connection connection = getConnection(em);
        DatabaseCleaner applicableCleaner = null;
        for (DatabaseCleaner.Factory factory : DATABASE_CLEANERS) {
            DatabaseCleaner cleaner = factory.create();
            if (cleaner.isApplicable(connection)) {
                applicableCleaner = cleaner;
                break;
            }
        }

        addIgnores(applicableCleaner);
        setLastDatabaseCleaner(applicableCleaner);
        return applicableCleaner;
    }

    @Before
    public void init() {
        boolean firstTest = lastTestClass != getClass();
        lastTestClass = getClass();
        // If a previous test run resolved the no-op cleaner, we won't be able to resolve any other cleaner
        if (resolvedNoop) {
            databaseCleaner = null;
            schemaChanged = true;
        } else {
            databaseCleaner = getLastDatabaseCleaner();
            schemaChanged = databaseCleaner == null;
        }

        // Nothing to delete if the schema changed
        databaseClean = schemaChanged;

        if (dataSource != null && recreateDataSource()) {
            dataSource.close();
            dataSource = null;
        }

        emf = createEntityManagerFactory("TestsuiteBase", createProperties("none"));
        em = emf.createEntityManager();

        // Disable query collecting
        QueryInspectorListener.enabled = false;
        QueryInspectorListener.collectSequences = false;

        if (!resolvedNoop && databaseCleaner == null) {
            DatabaseCleaner applicableCleaner = getDatabaseCleaner(em);

            if (applicableCleaner == null) {
                // If none was found, we use the default cleaner
                Logger.getLogger(getClass().getName()).warning("Could not resolve database cleaner for the database, falling back to drop-and-create strategy.");
                resolvedNoop = true;
            }
        }

        if (databaseCleaner == null) {
            // The default cleaner which recreates the schema
            setLastDatabaseCleaner(getDefaultDatabaseCleaner());
        }

        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config = configure(config);
        cbf = config.createCriteriaBuilderFactory(emf);
        jpaProvider = cbf.getService(JpaProvider.class);
        dbmsDialect = cbf.getService(DbmsDialect.class);

        if (schemaChanged || !databaseCleaner.supportsClearSchema()) {
            recreateOrClearSchema();
            setUpOnce();
        } else if (firstTest) {
            setUpOnce();
        }

        if (runTestInTransaction() && !em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    protected boolean runTestInTransaction() {
        return true;
    }

    protected void addIgnores(DatabaseCleaner applicableCleaner) {
        // No-op
    }

    protected void createSchema() {
        createEntityManagerFactory("TestsuiteBase", createProperties("create")).close();
    }

    protected void dropSchema() {
        createEntityManagerFactory("TestsuiteBase", createProperties("drop")).close();
    }

    protected void dropAndCreateSchema() {
        createEntityManagerFactory("TestsuiteBase", createProperties("drop-and-create")).close();
    }

    protected void recreateOrClearSchema() {
        if (databaseCleaner.supportsClearSchema()) {
            clearSchema();
            createSchema();
        } else {
            dropAndCreateSchema();
        }
    }

    protected void setUpOnce() {
        // No-op
    }

    protected abstract boolean supportsMapKeyDeReference();

    protected abstract boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined();

    protected boolean supportsMapInplaceUpdate() {
        return false;
    }

    protected boolean doesJpaMergeOfRecentlyPersistedEntityForceUpdate() {
        return true;
    }

    @After
    public void destruct() {
        EntityManagerFactory factory;
        // NOTE: We need to close the entity manager or else we could run into a deadlock on some dbms platforms
        // I am looking at you MySQL..
        if (em != null && em.isOpen()) {
            factory = em.getEntityManagerFactory();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        } else {
            factory = emf;
        }
        if (factory != null && factory.isOpen()) {
            factory.close();
        }

        if (databaseCleaner != null && !databaseCleaner.supportsClearSchema()) {
            dropSchema();
        }

        if (dataSource != null && recreateDataSource()) {
            dataSource.close();
            dataSource = null;
        }
    }

    protected Properties createProperties(String dbAction) {
        Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.url", System.getProperty("jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"));
        properties.put("javax.persistence.jdbc.user", System.getProperty("jdbc.user", "user"));
        properties.put("javax.persistence.jdbc.password", System.getProperty("jdbc.password", "password"));
        properties.put("javax.persistence.jdbc.driver", System.getProperty("jdbc.driver", "org.h2.Driver"));
        properties.put("javax.persistence.sharedCache.mode", "NONE");
        properties.put("javax.persistence.schema-generation.database.action", dbAction);
        properties = applyProperties(properties);
        return properties;
    }

    protected abstract Class<?>[] getEntityClasses();

    protected Connection getConnection(EntityManager em) {
        return em.unwrap(Connection.class);
    }
    
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
        persistenceUnitInfo.setNonJtaDataSource(getDataSource(properties));
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

        configurePersistenceUnitInfo(persistenceUnitInfo);
        return createEntityManagerFactory(persistenceUnitInfo, properties);
    }

    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
    }

    protected boolean supportsNestedEmbeddables() {
        return true;
    }

    protected boolean recreateDataSource() {
        return false;
    }

    private DataSource getDataSource(Map<Object, Object> properties) {
        if (dataSource != null) {
            // Remove properties that are normally removed
            properties.remove("javax.persistence.jdbc.driver");
            properties.remove("javax.persistence.jdbc.url");
            properties.remove("javax.persistence.jdbc.user");
            properties.remove("javax.persistence.jdbc.password");

            properties.remove("hibernate.connection.driver_class");
            properties.remove("hibernate.connection.url");
            properties.remove("hibernate.connection.username");
            properties.remove("hibernate.connection.password");
            return dataSource;
        }

        return dataSource = createPooledDataSource(proxyDataSource(createDataSource(properties)));
    }

    protected DataSource createDataSource(Map<Object, Object> properties) {
        try {
            // Load the driver
            Class.forName((String) properties.remove("javax.persistence.jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return createDataSource(
                (String) properties.remove("javax.persistence.jdbc.url"),
                (String) properties.remove("javax.persistence.jdbc.user"),
                (String) properties.remove("javax.persistence.jdbc.password")
        );
    }

    protected DataSource createDataSource(String url, String username, String password) {
        return new DataSourceImpl(url, username, password);
    }

    private HikariDataSource createPooledDataSource(DataSource dataSource) {
        HikariConfig config = new HikariConfig();
        // Need 3 connections, one for sequence table access, one for the test and another one for a possible write TX during a test
        config.setMaximumPoolSize(3);
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }

    private DataSource proxyDataSource(DataSource dataSource) {
        clearQueries();
        return ProxyDataSourceBuilder
                .create(dataSource)
                .listener(QueryInspectorListener.INSTANCE)
                .build();
    }

    private static final class QueryInspectorListener implements QueryExecutionListener {

        public static final QueryInspectorListener INSTANCE = new QueryInspectorListener();
        private static final List<String> EXECUTED_QUERIES = new ArrayList<>();
        private static boolean enabled = false;
        private static boolean collectSequences = false;

        private QueryInspectorListener() {
        }

        @Override
        public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        }

        @Override
        public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
            if (enabled) {
                for (QueryInfo q : list) {
                    String query = q.getQuery();
                    if (collectSequences || (!query.contains("next_val") && !query.contains("nextval"))) {
                        EXECUTED_QUERIES.add(query);
                    }
                }
            }
        }
    }

    public static void clearQueries() {
        QueryInspectorListener.EXECUTED_QUERIES.clear();
    }

    public static void enableQueryCollecting() {
        QueryInspectorListener.enabled = true;
    }

    public static void disableQueryCollecting() {
        QueryInspectorListener.enabled = false;
        QueryInspectorListener.EXECUTED_QUERIES.clear();
    }

    public static void assertUnorderedEquals(List<?> list1, List<?> list2) {
        assertEquals(list1.size(), list2.size());
        assertTrue(list1.containsAll(list2));
    }

    public static void assertQueryCount(int count) {
        List<String> queries = QueryInspectorListener.EXECUTED_QUERIES;
        if (count != queries.size()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected query count for queries:");
            for (String q : queries) {
                sb.append("\n").append(q);
            }
            Assert.assertEquals(
                    sb.toString(),
                    count,
                    queries.size());
        }
    }

    public AssertStatementBuilder assertQuerySequence() {
        return new AssertStatementBuilder(getRelationalModelAccessor(), QueryInspectorListener.EXECUTED_QUERIES);
    }

    public AssertStatementBuilder assertUnorderedQuerySequence() {
        return assertQuerySequence().unordered();
    }

    protected RelationalModelAccessor getRelationalModelAccessor() {
        return null;
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
