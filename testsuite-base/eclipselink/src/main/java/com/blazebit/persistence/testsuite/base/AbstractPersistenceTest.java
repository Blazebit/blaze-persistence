/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.sessions.factories.SessionManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
//        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
        properties.put("eclipselink.cache.shared.default", "false");
        properties.put("eclipselink.logging.logger", "JavaLogger");
        properties.put("eclipselink.logging.parameters", "true");
        // Disable the session part of the logging
        properties.put("eclipselink.logging.session", "false");
        // Give the session a easy name so we can have normal logging config
        properties.put("eclipselink.session-name", "default");
        properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, SchemaModifyingSessionCustomizer.class.getName());

        if (getSchemaMode() == SchemaMode.JPA) {
            SchemaModifyingSessionCustomizer.setSchemaName(getTargetSchema());
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
        applicableCleaner.addIgnoredTable("SEQUENCE");
    }

    @Override
    protected Connection getConnection(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        boolean startedTransaction = !tx.isActive();
        if (startedTransaction) {
            tx.begin();
        }
        return em.unwrap(Connection.class);
    }

    @Override
    protected void clearSchemaUsingJpa() {
        Map<String, EntityManagerSetupImpl> emSetupImpls = EntityManagerFactoryProvider.getEmSetupImpls();
        Map<String, EntityManagerSetupImpl> copy = new HashMap<>(emSetupImpls);
        emSetupImpls.clear();
        SessionManager manager = SessionManager.getManager();
        for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
            manager.getSessions().remove(emSetupImpl.getSessionName());
        }
        try {
            super.clearSchemaUsingJpa();
        } finally {
            emSetupImpls.putAll(copy);
            for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
                manager.addSession(emSetupImpl.getSession());
            }
        }
    }

    @Override
    protected EntityManagerFactory repopulateSchema() {
        Map<String, EntityManagerSetupImpl> emSetupImpls = EntityManagerFactoryProvider.getEmSetupImpls();
        Map<String, EntityManagerSetupImpl> copy = new HashMap<>(emSetupImpls);
        emSetupImpls.clear();
        SessionManager manager = SessionManager.getManager();
        for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
            manager.getSessions().remove(emSetupImpl.getSessionName());
        }
        try {
            return super.repopulateSchema();
        } finally {
            emSetupImpls.putAll(copy);
            for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
                manager.addSession(emSetupImpl.getSession());
            }
        }
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        return JpaProviderFamily.ECLIPSELINK;
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
