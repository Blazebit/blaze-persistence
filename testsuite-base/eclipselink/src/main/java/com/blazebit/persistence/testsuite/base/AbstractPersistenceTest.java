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

import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.sessions.Session;
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
 * @since 1.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
//        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
        properties.put("eclipselink.cache.shared.default", "false");
        properties.put("eclipselink.logging.level", "SEVERE");
//        properties.put("eclipselink.logging.level.sql", "FINE");
//        properties.put("eclipselink.logging.parameters", "true");
        return properties;
    }

    @Override
    protected Connection getConnection(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        boolean startedTransaction = !tx.isActive();
        try {
            if (startedTransaction) {
                tx.begin();
            }
            return em.unwrap(Connection.class);
        } finally {
            if (startedTransaction) {
                tx.commit();
            }
        }
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

    protected void dropAndCreateSchema() {
        Map<String, EntityManagerSetupImpl> emSetupImpls = EntityManagerFactoryProvider.getEmSetupImpls();
        Map<String, EntityManagerSetupImpl> copy = new HashMap<>(emSetupImpls);
        emSetupImpls.clear();
        SessionManager manager = SessionManager.getManager();
        for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
            manager.getSessions().remove(emSetupImpl.getSessionName());
        }
        try {
            super.dropAndCreateSchema();
        } finally {
            emSetupImpls.putAll(copy);
            for (EntityManagerSetupImpl emSetupImpl : copy.values()) {
                manager.addSession(emSetupImpl.getSession());
            }
        }
    }

}
