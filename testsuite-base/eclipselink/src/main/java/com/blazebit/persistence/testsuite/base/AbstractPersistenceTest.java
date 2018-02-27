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

package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.sessions.factories.SessionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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
    protected void dropSchema() {
        Map<String, EntityManagerSetupImpl> emSetupImpls = EntityManagerFactoryProvider.getEmSetupImpls();
        Map<String, EntityManagerSetupImpl> copy = new HashMap<>(emSetupImpls);
        emSetupImpls.clear();
        SessionManager manager = SessionManager.getManager();
        for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
            manager.getSessions().remove(emSetupImpl.getSessionName());
        }
        try {
            super.dropSchema();
        } finally {
            emSetupImpls.putAll(copy);
            for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
                manager.addSession(emSetupImpl.getSession());
            }
        }
    }

    protected void recreateOrClearSchema() {
        Map<String, EntityManagerSetupImpl> emSetupImpls = EntityManagerFactoryProvider.getEmSetupImpls();
        Map<String, EntityManagerSetupImpl> copy = new HashMap<>(emSetupImpls);
        emSetupImpls.clear();
        SessionManager manager = SessionManager.getManager();
        for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
            manager.getSessions().remove(emSetupImpl.getSessionName());
        }
        try {
            super.recreateOrClearSchema();
        } finally {
            emSetupImpls.putAll(copy);
            for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
                manager.addSession(emSetupImpl.getSession());
            }
        }
    }

}
