/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.MutablePersistenceUnitInfo;
import com.blazebit.persistence.testsuite.base.jpa.RelationalModelAccessor;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import org.hibernate.Version;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.entity.AbstractEntityPersister;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;


/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    private static final int HIBERNATE_MAJOR_VERSION;
    private static final int HIBERNATE_MINOR_VERSION;

    static {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        HIBERNATE_MAJOR_VERSION = Integer.parseInt(versionParts[0]);
        HIBERNATE_MINOR_VERSION = Integer.parseInt(versionParts[1]);
        // This is pretty hackish but necessary to be able to execute tests with Hibernate before 5.2 as well
        // We have an empty stub interface class org.hibernate.engine.spi.SharedSessionContractImplementor to be able to
        // define user types that are compatible with all Hibernate versions but must ensure that on 5.2+ we load the correct class
        // So we find the class file and load that class file into the class loader
        if (HIBERNATE_MAJOR_VERSION > 5 || HIBERNATE_MINOR_VERSION > 1) {
            try {
                URL correctClass = null;
                ClassLoader classLoader = Version.class.getClassLoader();
                List<URL> list = Collections.list(classLoader.getResources("org/hibernate/engine/spi/SharedSessionContractImplementor.class"));
                if (list.size() > 1) {
                    for (URL url : list) {
                        if (url.toString().contains(".jar")) {
                            correctClass = url;
                        }
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    try (InputStream inputStream = correctClass.openStream()) {
                        int r;
                        while ((r = inputStream.read(buffer)) != -1) {
                            baos.write(buffer, 0, r);
                        }
                    }
                    byte[] bytes = baos.toByteArray();
                    try {
                        Method m = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
                        // We need a class from the same package
                        MethodHandles.Lookup lookup = (MethodHandles.Lookup) m.invoke(null, Status.class, MethodHandles.lookup());
                        MethodHandles.Lookup.class.getMethod("defineClass", byte[].class).invoke(lookup, (Object) bytes);
                    } catch (NoSuchMethodException ex) {
                        Field f;
                        try {
                            f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
                            f.setAccessible(true);
                            Object unsafe = f.get(null);
                            Method defineClass = Class.forName("sun.misc.Unsafe").getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                            defineClass.invoke(unsafe, "org.hibernate.engine.spi.SharedSessionContractImplementor", bytes, 0, bytes.length, classLoader, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    protected Connection getConnection(EntityManager em) {
        return em.unwrap(SessionImplementor.class).connection();
    }

    @Override
    protected void addIgnores(DatabaseCleaner applicableCleaner) {
        applicableCleaner.addIgnoredTable("hibernate_sequence");
    }

    @Override
    protected Properties applyProperties(Properties properties) {
        boolean isMySql = properties.get("javax.persistence.jdbc.url").toString().contains("mysql");
        if (System.getProperty("hibernate.dialect") != null) {
            properties.put("hibernate.dialect", System.getProperty("hibernate.dialect"));
        } else if (isMySql) {
            // MySQL is drunk, it does stuff case insensitive by default...
            properties.put("hibernate.dialect", SaneMySQLDialect.class.getName());
            
            // Since MySQL has no sequences, the native strategy is needed for batch inserts
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("db2")) {
            // The original DB2 dialect misses support for sequence retrieve in select statements
            properties.put("hibernate.dialect", SaneDB2Dialect.class.getName());
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("h2")) {
            // Hibernate 5 uses sequences by default but h2 seems to have a bug with sequences in a limited query
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("sqlserver")) {
            // Apparently the dialect resolver doesn't choose the latest dialect
            properties.put("hibernate.dialect", SQLServer2012Dialect.class.getName());
            // Not sure what is happening, but when the sequence is tried to be fetched, it doesn't exist in SQL Server
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        } else if (isHibernate5() && properties.get("javax.persistence.jdbc.url").toString().contains("oracle")) {
            // Apparently the dialect resolver doesn't choose the latest dialect
            properties.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
        }
        String targetSchema = System.getProperty("hibernate.default_schema", getTargetSchema());
        if (targetSchema != null && getSchemaMode() == SchemaMode.JPA) {
            properties.put("hibernate.default_schema", targetSchema);
        }
        if (useHbm2ddl()) {
            properties.put("hibernate.connection.url", properties.remove("javax.persistence.jdbc.url"));
            properties.put("hibernate.connection.password", properties.remove("javax.persistence.jdbc.password"));
            properties.put("hibernate.connection.username", properties.remove("javax.persistence.jdbc.user"));
            properties.put("hibernate.connection.driver_class", properties.remove("javax.persistence.jdbc.driver"));
            String dbAction = (String) properties.remove("javax.persistence.schema-generation.database.action");
            if ("drop-and-create".equals(dbAction)) {
                properties.put("hibernate.hbm2ddl.auto", "create");
            } else if ("create".equals(dbAction)) {
                properties.put("hibernate.hbm2ddl.auto", "create");
            } else if ("drop".equals(dbAction)) {
                // That's the best we can do
                properties.put("hibernate.hbm2ddl.auto", "create-drop");
            } else if ("none".equals(dbAction)) {
                properties.put("hibernate.hbm2ddl.auto", "none");
            } else {
                throw new IllegalArgumentException("Unsupported database action: " + dbAction);
            }
        }

        if (isHibernate526OrOlder()) {
            // Disable in <= 5.2.6 since it's still broken
            properties.put("hibernate.collection_join_subquery", "false");
        }

        // Needed for Envers tests in Hibernate >= 5.3.5, 5.4.x (HHH-12871)
        properties.put("hibernate.ejb.metamodel.population", "enabled");

        if (isHibernate53Or54()) {
            properties.put("hibernate.archive.scanner", "org.hibernate.boot.archive.scan.internal.DisabledScanner");
        }

        // We use the following only for debugging purposes
        // Normally these settings should be disabled since the output would be too big TravisCI
//        properties.put("hibernate.show_sql", "true");
//        properties.put("hibernate.format_sql", "true");
        return properties;
    }

    @Override
    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        if (!supportsNestedEmbeddables() && containsDocumentEntity(getEntityClasses())) {
            persistenceUnitInfo.addMappingFileName("META-INF/override-embeddables-orm.xml");
        }
    }

    @Override
    protected boolean supportsNestedEmbeddables() {
        return !isHibernate4();
    }

    @Override
    public boolean supportsTableGroupJoins() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major > 5 || (major == 5 && (minor > 2 || (minor == 2 && fix >= 8)));
    }

    protected boolean doesJpaMergeOfRecentlyPersistedEntityForceUpdate() {
        // Not sure when exactly this got fixed, but 5.1 doesn't seem to have that problem
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major < 5 || major == 5 && minor < 1 || major == 5 && minor == 1 && fix < 0;
    }

    protected boolean supportsIndexedInplaceUpdate() {
        // Hibernate 4 doesn't support inplace updates
        return isHibernate5();
    }

    private boolean containsDocumentEntity(Class<?>[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if ("Document".equals(classes[i].getSimpleName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected DataSource createDataSource(Map<Object, Object> properties, Consumer<Connection> connectionCustomizer) {
        if (!useHbm2ddl()) {
            return super.createDataSource(properties, connectionCustomizer);
        }

        try {
            // Load the driver
            Class.forName((String) properties.remove("hibernate.connection.driver_class"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return createDataSource(
                (String) properties.remove("hibernate.connection.url"),
                (String) properties.remove("hibernate.connection.username"),
                (String) properties.remove("hibernate.connection.password"),
                connectionCustomizer
        );
    }

    @Override
    protected RelationalModelAccessor getRelationalModelAccessor() {
        return new RelationalModelAccessor() {
            @Override
            public String tableFromEntity(Class<?> entityClass) {
                SessionImplementor session = em.unwrap(SessionImplementor.class);
                AbstractEntityPersister persister = (AbstractEntityPersister) session.getFactory().getEntityPersister(entityClass.getName());
                return persister.getTableName().substring(persister.getTableName().lastIndexOf('.') + 1);
            }

            @Override
            public String tableFromEntityRelation(Class<?> entityClass, String relationName) {
                JoinTable joinTable = jpaProvider.getJoinTable(em.getMetamodel().entity(entityClass), relationName);
                if (joinTable != null) {
                    return joinTable.getTableName().substring(joinTable.getTableName().lastIndexOf('.') + 1);
                }
                return null;
            }
        };
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        // Only got introduced in 5.2.8
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major > 5 || major == 5 && minor > 2 || major == 5 && minor == 2 && fix > 7;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        // Apparently this got fixed in Hibernate 5
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        return major >= 5;
    }

    private boolean isHibernate4() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        return major == 4;
    }

    private boolean useHbm2ddl() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        return major == 4 && minor <= 3;
    }

    private boolean isHibernate5() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        return major >= 5;
    }

    private boolean isHibernate526OrOlder() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major < 5 || major == 5 && minor < 2 || major == 5 && minor == 2 && fix < 7;
    }

    private boolean isHibernate53Or54() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("[\\.-]");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        return major == 5 && (minor == 3 || minor == 4);
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        return JpaProviderFamily.HIBERNATE;
    }

    @Override
    protected int getJpaProviderMajorVersion() {
        return HIBERNATE_MAJOR_VERSION;
    }

    @Override
    protected int getJpaProviderMinorVersion() {
        return HIBERNATE_MINOR_VERSION;
    }
}
