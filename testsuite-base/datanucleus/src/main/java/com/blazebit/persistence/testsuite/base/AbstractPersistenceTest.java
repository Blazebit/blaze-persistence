/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import org.datanucleus.ExecutionContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ConnectionManager;
import org.datanucleus.store.connection.ManagedConnection;
import org.junit.Before;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    // We need to recreate the emf because Datanucleus uses messed up metadata otherwise
    @Before
    public void recreateEmf() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
        em = null;
        init();
    }

    @Override
    protected Properties applyProperties(Properties properties) {
        properties.put("datanucleus.rdbms.mysql.characterSet", "utf8mb3");
        properties.put("datanucleus.rdbms.mysql.collation", "utf8mb3_bin");

        String targetSchema = getTargetSchema();
        if (targetSchema != null && getSchemaMode() == SchemaMode.JPA) {
            properties.put("datanucleus.mapping.Schema", targetSchema);
        }
        return properties;
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
    protected void addIgnores(DatabaseCleaner applicableCleaner) {
        applicableCleaner.addIgnoredTable("SEQUENCE_TABLE");
    }

    @Override
    protected Connection getConnection(EntityManager em) {
        StoreManager storeManager = em.unwrap(StoreManager.class);
        ExecutionContext ec = em.unwrap(ExecutionContext.class);
        try {
            // Datanucleus 5.1 changed the API
            Method getConnection = ConnectionManager.class.getMethod("getConnection", ExecutionContext.class);
            ConnectionManager connectionManager = storeManager.getConnectionManager();
            return (Connection) ((ManagedConnection) getConnection.invoke(connectionManager, ec)).getConnection();
        } catch (NoSuchMethodException ex) {
            return (Connection) storeManager.getConnection(ec).getConnection();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        return JpaProviderFamily.DATANUCLEUS;
    }

    @Override
    protected int getJpaProviderMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int getJpaProviderMinorVersion() {
        throw new UnsupportedOperationException();
    }
}
