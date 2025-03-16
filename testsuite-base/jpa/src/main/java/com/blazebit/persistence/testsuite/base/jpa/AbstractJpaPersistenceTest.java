/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.CriteriaBuilderConfigurationImpl;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DB2DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.H2DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.MSSQLDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.MySQL5DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.MySQL8DatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.OracleDatabaseCleaner;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.PostgreSQLDatabaseCleaner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.After;
import org.junit.AfterClass;
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
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractJpaPersistenceTest {

    protected static EntityManagerFactory emf;
    protected static CriteriaBuilderFactory cbf;
    protected static JpaProvider jpaProvider;
    protected static DbmsDialect dbmsDialect;
    private static boolean resolvedNoop = false;
    private static boolean databaseClean = false;
    private static Class<?> lastTestClass;
    private static String lastTargetSchema;
    private static Class<?> recreateTestClass;
    private static Set<Class<?>> databaseCleanerClasses;
    private static DatabaseCleaner databaseCleaner;
    private static HikariDataSource dataSource;
    private static DataSource bootstrapDataSource;
    private static CriteriaBuilderConfigurationEqualityWrapper lastCriteriaBuilderConfigurationEqualityWrapper;
    private static SchemaMode lastSchemaMode;
    private static CriteriaBuilderConfiguration defaultCbConfiguration;
    private static CriteriaBuilderConfiguration activeCriteriaBuilderConfiguration;
    private static final Map<Class<?>, List<String>> PLURAL_DELETES = new HashMap<>();
    private static final List<DatabaseCleaner.Factory> DATABASE_CLEANERS = Arrays.asList(
            new H2DatabaseCleaner.Factory(),
            new PostgreSQLDatabaseCleaner.Factory(),
            new DB2DatabaseCleaner.Factory(),
            new MySQL5DatabaseCleaner.Factory(),
            new MySQL8DatabaseCleaner.Factory(),
            new MSSQLDatabaseCleaner.Factory(),
            new OracleDatabaseCleaner.Factory()
    );

    protected EntityManager em;

    static {
        System.setProperty("org.jboss.logging.provider", "jdk");

        Locale.setDefault(new Locale(
                System.getProperty("user.language", "en"),
                System.getProperty("user.country", "US")
        ));
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
        cleanDatabaseWithCleaner();
    }

    protected final void cleanDatabaseWithCleaner() {
        // Nothing to delete if the database is "clean"
        if (databaseClean) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            boolean wasAutoCommit = false;
            try {
                // Turn off auto commit if necessary
                wasAutoCommit = connection.getAutoCommit();
                if (wasAutoCommit) {
                    connection.setAutoCommit(false);
                }
                // Clear the data with the cleaner
                String targetSchema = getTargetSchema();
                if (targetSchema == null) {
                    targetSchema = connection.getSchema();
                }
                if (targetSchema == null) {
                    databaseCleaner.clearAllData(connection);
                } else {
                    databaseCleaner.clearData(connection, targetSchema);
                }
                databaseClean = true;
            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    ex.addSuppressed(e1);
                }

                throw new RuntimeException(ex);
            } finally {
                connection.commit();
                if (wasAutoCommit) {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        clearSchema(databaseCleaner);
    }

    public void clearSchema(DatabaseCleaner databaseCleaner) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            // Clear the data with the cleaner
            String targetSchema = getTargetSchema();
            if (targetSchema == null) {
                databaseCleaner.clearAllSchemas(connection);
            } else {
                databaseCleaner.clearSchema(connection, targetSchema);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            public void clearAllSchemas(Connection connection) {
            }

            @Override
            public void clearSchema(Connection connection, String schemaName) {
            }

            @Override
            public void addIgnoredTable(String tableName) {
            }

            @Override
            public void clearAllData(Connection connection) {
                repopulateSchema();
            }

            @Override
            public void clearData(Connection connection, String schemaName) {
                repopulateSchema();
            }

            @Override
            public void createDatabaseIfNotExists(Connection connection, String databaseName) {
            }

            @Override
            public void createSchemaIfNotExists(Connection connection, String schemaName) {
            }

            @Override
            public void applyTargetDatabasePropertyModifications(Map<Object, Object> properties, String databaseName) {
            }

            @Override
            public void applyTargetSchemaPropertyModifications(Map<Object, Object> properties, String schemaName) {
            }
        };
    }

    public DatabaseCleaner getDatabaseCleaner(Connection connection) {
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
        boolean veryFirstTest = lastTestClass == null;
        boolean schemaChanged;
        lastTestClass = getClass();
        // If a previous test run resolved the no-op cleaner, we won't be able to resolve any other cleaner
        if (resolvedNoop) {
            databaseCleaner = null;
            schemaChanged = true;
        } else {
            databaseCleaner = getLastDatabaseCleaner();
            schemaChanged = databaseCleaner == null || !Objects.equals(lastTargetSchema, getTargetSchema());
        }
        lastTargetSchema = getTargetSchema();

        SchemaMode schemaMode = getSchemaMode();
        if (dataSource != null && lastSchemaMode != schemaMode) {
            dataSource.close();
            dataSource = null;
            closeEmf();
        }
        lastSchemaMode = schemaMode;

        // Nothing to delete if the schema changed
        databaseClean = schemaChanged;

        if (dataSource != null && recreateDataSource()) {
            recreateTestClass = getClass();
            dataSource.close();
            dataSource = null;
            closeEmf();
        } else if (recreateTestClass != null && recreateTestClass != getClass()) {
            recreateTestClass = null;
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
            closeEmf();
        }

        // Disable query collecting
        QueryInspectorListener.enabled = false;
        QueryInspectorListener.collectSequences = false;

        if (!resolvedNoop && databaseCleaner == null) {
            if (bootstrapDataSource == null) {
                getBootstrapDataSource();
            }
            try (Connection c = bootstrapDataSource.getConnection()) {
                DatabaseCleaner applicableCleaner = getDatabaseCleaner(c);

                if (applicableCleaner == null) {
                    // If none was found, we use the default cleaner
                    Logger.getLogger(getClass().getName()).warning("Could not resolve database cleaner for the database, falling back to drop-and-create strategy.");
                    resolvedNoop = true;
                }
                String targetDatabase = getTargetDatabase();
                String targetSchema = getTargetSchema();
                if (targetDatabase != null) {
                    databaseCleaner.createDatabaseIfNotExists(c, targetDatabase);
                }
                if (targetSchema != null) {
                    createSchemaIfNotExists(c, targetSchema);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (databaseCleaner == null) {
            // The default cleaner which recreates the schema
            setLastDatabaseCleaner(getDefaultDatabaseCleaner());
        }

        EntityManagerFactory emfBefore = emf;
        if (veryFirstTest || schemaChanged) {
            getDataSource(createProperties("none"));
            emf = repopulateSchema();
            if (emf == null) {
                emf = createEntityManagerFactory("TestsuiteBase", createProperties("none"));
            }
        } else if (emf == null || !emf.isOpen()) {
            emf = createEntityManagerFactory("TestsuiteBase", createProperties("none"));
        }

        if (activeCriteriaBuilderConfiguration == null) {
            if (requiresCriteriaBuilderConfigurationCustomization()) {
                activeCriteriaBuilderConfiguration = Criteria.getDefault();
                configure(activeCriteriaBuilderConfiguration);
            } else {
                if (defaultCbConfiguration == null) {
                    // We cannot initialize this statically due to static initializer in
                    // com.blazebit.persistence.testsuite.base.AbstractPersistenceTest (Hibernate)
                    defaultCbConfiguration = Criteria.getDefault();
                }
                activeCriteriaBuilderConfiguration = defaultCbConfiguration;
            }
        }

        CriteriaBuilderConfigurationEqualityWrapper cfgEqualityWrapper = new CriteriaBuilderConfigurationEqualityWrapper(
                (CriteriaBuilderConfigurationImpl) activeCriteriaBuilderConfiguration
        );
        if (cbf == null || emfBefore != emf || !cfgEqualityWrapper.equals(lastCriteriaBuilderConfigurationEqualityWrapper)) {
            cbf = activeCriteriaBuilderConfiguration.createCriteriaBuilderFactory(emf);
            lastCriteriaBuilderConfigurationEqualityWrapper = cfgEqualityWrapper;
            jpaProvider = cbf.getService(JpaProvider.class);
            dbmsDialect = cbf.getService(DbmsDialect.class);
        }

        getEm();
        if (firstTest) {
            setUpOnce();
        }

        if (runTestInTransaction() && !getEm().getTransaction().isActive()) {
            getEm().getTransaction().begin();
        }
    }

    protected void createSchemaIfNotExists(Connection connection, String schemaName) {
        databaseCleaner.createSchemaIfNotExists(connection, schemaName);
    }

    protected SchemaMode getSchemaMode() {
        return SchemaMode.JPA;
    }

    private EntityManager getEm() {
        if (em == null) {
            em = emf.createEntityManager();
        }
        return em;
    }

    protected boolean runTestInTransaction() {
        return true;
    }

    protected void addIgnores(DatabaseCleaner applicableCleaner) {
        // No-op
    }

    protected boolean needsEntityManagerForDbAction() {
        return false;
    }

    protected static void resetTimeZoneCaches() {
        // The H2 JDBC driver is not able to handle timezone changes because of an internal cache
        try {
            Class.forName("org.h2.util.DateTimeUtils").getMethod("resetCalendar").invoke(null);
        } catch (Exception e) {
            // Ignore any exceptions. If it is H2 it will succeed, otherwise will fail on class lookup already
        }

        // EclipseLink caches the timezone so we have to purge that cache
        try {
            Class<?> helperClass = Class.forName("org.eclipse.persistence.internal.helper.Helper");
            Field f = helperClass.getDeclaredField("defaultTimeZone");
            f.setAccessible(true);
            f.set(null, TimeZone.getDefault());

            f = helperClass.getDeclaredField("calendarCache");
            f.setAccessible(true);
            f.set(null, helperClass.getMethod("initCalendarCache").invoke(null));
        } catch (Exception e) {
            // Ignore any exceptions. If it is EclipseLink it will succeed, otherwise will fail on class lookup already
        }
    }

    protected EntityManagerFactory populateSchema() {
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory("TestsuiteBase", createProperties("create"));
        if (needsEntityManagerForDbAction()) {
            entityManagerFactory.createEntityManager().close();
            entityManagerFactory = null;
        }
        return entityManagerFactory;
    }

    protected void clearSchemaUsingJpa() {
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory("TestsuiteBase", createProperties("drop"));
        if (needsEntityManagerForDbAction()) {
            try {
                entityManagerFactory.createEntityManager().close();
            } finally {
                entityManagerFactory.close();
            }
        }
    }

    private EntityManagerFactory repopulateSchemaUsingJpa() {
        EntityManagerFactory entityManagerFactory = createEntityManagerFactory("TestsuiteBase", createProperties("drop-and-create"));
        if (needsEntityManagerForDbAction()) {
            entityManagerFactory.createEntityManager().close();
            entityManagerFactory = null;
        }
        return entityManagerFactory;
    }

    protected EntityManagerFactory repopulateSchema() {
        if (databaseCleaner.supportsClearSchema()) {
            clearSchema();
            return populateSchema();
        } else {
            return repopulateSchemaUsingJpa();
        }
    }

    protected void setUpOnce() {
        // No-op
    }

    protected abstract boolean supportsMapKeyDeReference();

    protected abstract boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined();

    // Hibernate before 6 did not properly fetch elements of a natural id mapped collection
    protected boolean supportsSingleStatementNaturalIdCollectionFetching() {
        return false;
    }

    // As of Hibernate 6, we don't always need to join the element table
    protected boolean supportsLazyCollectionElementJoin() {
        return false;
    }

    // Hibernate before 6 always initialized a proxy for the remove operation
    protected boolean supportsProxyRemoveWithoutLoading() {
        return false;
    }

    protected boolean supportsIndexedInplaceUpdate() {
        return false;
    }

    public boolean supportsTableGroupJoins() {
        return false;
    }

    protected boolean supportsAdvancedSql() {
        ExtendedQuerySupport extendedQuerySupport = cbf.getService(ExtendedQuerySupport.class);
        return extendedQuerySupport != null && extendedQuerySupport.supportsAdvancedSql();
    }

    protected boolean doesJpaMergeOfRecentlyPersistedEntityForceUpdate() {
        return true;
    }

    protected boolean doesTransientCheckBeforeFlush() {
        return false;
    }

    @After
    public void destruct() {
        // NOTE: We need to close the entity manager or else we could run into a deadlock on some dbms platforms
        // I am looking at you MySQL..
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            em = null;
        }

        if (databaseCleaner != null && !databaseCleaner.supportsClearSchema()) {
            clearSchemaUsingJpa();
        }

        if (dataSource != null && recreateDataSource()) {
            dataSource.close();
            dataSource = null;
            closeEmf();
        }
    }

    public static void closeEmf() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }

    @AfterClass
    public static void tearDownClass() {
        closeEmf();
        activeCriteriaBuilderConfiguration = null;
    }

    protected String getTargetSchema() {
        return System.getProperty("jdbc.schema");
    }

    protected String getTargetDatabase() {
        return System.getProperty("jdbc.database");
    }

    protected Properties createProperties(String dbAction) {
        Properties properties = createDefaultProperties();
        properties.put("javax.persistence.schema-generation.database.action", dbAction);
        String targetDatabase = getTargetDatabase();
        String targetSchema = getTargetSchema();
        if (targetDatabase != null) {
            databaseCleaner.applyTargetDatabasePropertyModifications(properties, targetDatabase);
        }
        if (targetSchema != null && getSchemaMode() == SchemaMode.JDBC) {
            databaseCleaner.applyTargetSchemaPropertyModifications(properties, targetSchema);
        }
        return applyProperties(properties);
    }

    protected Properties createBootstrapProperties() {
        Properties properties = createDefaultProperties();
        return applyProperties(properties);
    }

    private static Properties createDefaultProperties() {
        Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.url", System.getProperty("jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"));
        properties.put("javax.persistence.jdbc.user", System.getProperty("jdbc.user", "user"));
        properties.put("javax.persistence.jdbc.password", System.getProperty("jdbc.password", "password"));
        properties.put("javax.persistence.jdbc.driver", System.getProperty("jdbc.driver", "org.h2.Driver"));
        properties.put("javax.persistence.sharedCache.mode", "NONE");
        properties.put("javax.persistence.schema-generation.database.action", "none");
        return properties;
    }

    protected abstract Class<?>[] getEntityClasses();

    protected Connection getConnection(EntityManager em) {
        return em.unwrap(Connection.class);
    }
    
    protected boolean requiresCriteriaBuilderConfigurationCustomization() {
        return false;
    }

    protected void configure(CriteriaBuilderConfiguration config) { }

    protected Properties applyProperties(Properties properties) {
        return properties;
    }

    protected EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map<Object, Object> properties) {
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

    protected boolean supportsCollectionTableCteDelete() {
        return false;
    }

    protected boolean optimizesUnnecessaryCasts() {
        return false;
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

        return dataSource = createPooledDataSource(proxyDataSource(createDataSource(properties, null)));
    }

    private DataSource getBootstrapDataSource() {
        if (bootstrapDataSource == null) {
            return bootstrapDataSource = createDataSource(createBootstrapProperties(), null);
        }
        return bootstrapDataSource;
    }

    protected DataSource createDataSource(Map<Object, Object> properties, Consumer<Connection> connectionCustomizer) {
        try {
            // Load the driver
            Class.forName((String) properties.remove("javax.persistence.jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return createDataSource(
                (String) properties.remove("javax.persistence.jdbc.url"),
                (String) properties.remove("javax.persistence.jdbc.user"),
                (String) properties.remove("javax.persistence.jdbc.password"),
                connectionCustomizer
        );
    }

    protected DataSource createDataSource(String url, String username, String password, Consumer<Connection> connectionCustomizer) {
        return new DataSourceImpl(url, username, password, connectionCustomizer);
    }

    private HikariDataSource createPooledDataSource(DataSource dataSource) {
        HikariConfig config = new HikariConfig();
        // Need 3 connections, one for sequence table access, one for the test and another one for a possible write TX during a test
        config.setMaximumPoolSize(3);
        config.setDataSource(dataSource);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(config);
    }

    private DataSource proxyDataSource(DataSource dataSource) {
        clearQueries();
        return ProxyDataSourceBuilder
                .create(dataSource)
                .listener(QueryInspectorListener.INSTANCE)
                .build();
    }

    protected static final class QueryInspectorListener implements QueryExecutionListener {

        public static final QueryInspectorListener INSTANCE = new QueryInspectorListener();
        public static final List<String> EXECUTED_QUERIES = new ArrayList<>();
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
                    if (collectSequences || (!query.contains("next_val") && !query.contains("nextval") && !query.contains("next value for"))) {
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

    protected static <T, E extends Throwable> E verifyException(T object, Consumer<T> action) {
        return (E) verifyException(object, Throwable.class, action);
    }

    protected static <T, E extends Throwable> E verifyException(T object, Class<E> throwableClass, Consumer<T> action) {
        try {
            action.accept(object);
            throw new AssertionError("Neither an exception of type " + throwableClass.getName() + " nor another exception was thrown");
        } catch (Throwable ex) {
            if (!throwableClass.isInstance(ex)) {
                throw new AssertionError("Exception of type " + throwableClass.getName() + " expected but was not thrown. " + "Instead an exception of type " + ex.getClass() + " with message '" + ex.getMessage() + "' was thrown.", ex);
            }
            return (E) ex;
        }
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
        if (failures.size() <= 1 || failures.size() == 2 && failures.containsKey("org.hibernate.ejb.HibernatePersistence") && failures.containsKey("org.hibernate.jpa.HibernatePersistenceProvider")) {
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

    protected abstract JpaProviderFamily getJpaProviderFamily();

    protected abstract int getJpaProviderMajorVersion();

    protected abstract int getJpaProviderMinorVersion();

    protected enum SchemaMode {
        JPA,
        JDBC
    }

    protected enum JpaProviderFamily {
        HIBERNATE,
        DATANUCLEUS,
        ECLIPSELINK,
        OPENJPA
    }

    private static class CriteriaBuilderConfigurationEqualityWrapper {
        private final Properties properties;
        private final Map<String, Class<?>> macros;
        private final Map<String, Class<?>> functions;

        private CriteriaBuilderConfigurationEqualityWrapper(CriteriaBuilderConfigurationImpl cfg) {
            this.properties = cfg.getProperties();
            this.macros = cfg.getMacros().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getClass()));
            this.functions = cfg.getFunctions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getClass()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CriteriaBuilderConfigurationEqualityWrapper that = (CriteriaBuilderConfigurationEqualityWrapper) o;
            return properties.equals(that.properties) &&
                    macros.equals(that.macros) &&
                    functions.equals(that.functions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, macros, functions);
        }
    }
}
