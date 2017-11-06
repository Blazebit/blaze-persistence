/*
 * Copyright 2014 - 2017 Blazebit.
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

import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;


/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Connection getConnection(EntityManager em) {
        return em.unwrap(SessionImplementor.class).connection();
    }

    @Override
    protected Properties applyProperties(Properties properties) {
        if (System.getProperty("hibernate.dialect") != null) {
            properties.put("hibernate.dialect", System.getProperty("hibernate.dialect"));
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("mysql")) {
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
        
        // We use the following only for debugging purposes
        // Normally these settings should be disabled since the output would be too big TravisCI
//        properties.put("hibernate.show_sql", "true");
//        properties.put("hibernate.format_sql", "true");
        return properties;
    }

    @Override
    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        if (!supportsNestedEmbeddables() && containsDocumentEntity(getEntityClasses())) {
            persistenceUnitInfo.addMappingFileName("META-INF/orm.xml");
        }
    }

    @Override
    protected boolean supportsNestedEmbeddables() {
        return !isHibernate4();
    }

    protected boolean doesJpaMergeOfRecentlyPersistedEntityForceUpdate() {
        // Not sure when exactly this got fixed, but 5.1 doesn't seem to have that problem
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major < 5 || major == 5 && minor < 1 || major == 5 && minor == 1 && fix < 0;
    }

    protected boolean supportsMapInplaceUpdate() {
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
    protected DataSource createDataSource(Map<Object, Object> properties) {
        if (!useHbm2ddl()) {
            return super.createDataSource(properties);
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
                (String) properties.remove("hibernate.connection.password")
        );
    }

    @Override
    protected RelationalModelAccessor getRelationalModelAccessor() {
        return new RelationalModelAccessor() {
            @Override
            public String tableFromEntity(Class<?> entityClass) {
                SessionImplementor session = em.unwrap(SessionImplementor.class);
                AbstractEntityPersister persister = (AbstractEntityPersister) session.getFactory().getEntityPersister(entityClass.getName());
                return persister.getTableName();
            }

            @Override
            public String tableFromEntityRelation(Class<?> entityClass, String relationName) {
                return jpaProvider.getJoinTable(em.getMetamodel().entity(entityClass), relationName);
            }
        };
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        // Only got introduced in 5.2.8
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major > 5 || major == 5 && minor > 2 || major == 5 && minor == 2 && fix > 7;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        // Apparently this got fixed in Hibernate 5
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        return major >= 5;
    }

    private boolean isHibernate4() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        return major == 4;
    }

    private boolean useHbm2ddl() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        return major == 4 && minor <= 3;
    }

    private boolean isHibernate5() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        return major >= 5;
    }

    private boolean isHibernate526OrOlder() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        return major < 5 || major == 5 && minor < 2 || major == 5 && minor == 2 && fix < 7;
    }
}
