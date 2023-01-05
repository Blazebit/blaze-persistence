/*
 * Copyright 2014 - 2023 Blazebit.
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
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.entity.AbstractEntityPersister;

import jakarta.persistence.EntityManager;
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
import java.util.Properties;


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

    @Override
    protected Connection getConnection(EntityManager em) {
        return em.unwrap(SessionImplementor.class).getJdbcCoordinator().getLogicalConnection().getPhysicalConnection();
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
            // Since MySQL has no sequences, the native strategy is needed for batch inserts
            properties.put("hibernate.id.new_generator_mappings", "false");
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("h2")) {
            // Hibernate 5 uses sequences by default but h2 seems to have a bug with sequences in a limited query
            properties.put("hibernate.id.new_generator_mappings", "false");
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("sqlserver")) {
            // Not sure what is happening, but when the sequence is tried to be fetched, it doesn't exist in SQL Server
            properties.put("hibernate.id.new_generator_mappings", "false");
        }
        String targetSchema = System.getProperty("hibernate.default_schema", getTargetSchema());
        // Ignore default schema for MySQL since MySQL does not support schemas and bad things happen in some Hibernate
        // versions if we still set it.
        if (targetSchema != null && getSchemaMode() == SchemaMode.JPA) {
            properties.put("hibernate.default_schema", targetSchema);
        }

        // Needed for Envers tests in Hibernate >= 5.3.5, 5.4.x (HHH-12871)
        properties.put("hibernate.ejb.metamodel.population", "enabled");
        properties.put("hibernate.jpa.metamodel.population", "enabled");

        // Continue using hibernate_sequence also in Hibernate 6 because InsertTest depends on it
        // If we were to use optimizable sequences, we would require multi-table statements which are unsupported
        properties.put("hibernate.id.db_structure_naming_strategy", "legacy");
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
        return true;
    }

    @Override
    public boolean supportsTableGroupJoins() {
        return true;
    }

    protected boolean doesJpaMergeOfRecentlyPersistedEntityForceUpdate() {
        return false;
    }

    protected boolean supportsIndexedInplaceUpdate() {
        return true;
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
    protected RelationalModelAccessor getRelationalModelAccessor() {
        return new RelationalModelAccessor() {
            @Override
            public String tableFromEntity(Class<?> entityClass) {
                SessionImplementor session = em.unwrap(SessionImplementor.class);
                AbstractEntityPersister persister = (AbstractEntityPersister) session.getFactory().getMappingMetamodel().getEntityDescriptor(entityClass.getName());
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
        return true;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        return true;
    }

    @Override
    protected boolean supportsSingleStatementNaturalIdCollectionFetching() {
        return true;
    }

    @Override
    protected boolean supportsLazyCollectionElementJoin() {
        return true;
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
