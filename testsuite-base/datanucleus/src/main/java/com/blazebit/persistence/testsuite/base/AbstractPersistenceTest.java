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
import org.datanucleus.ExecutionContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ConnectionManager;
import org.datanucleus.store.connection.ManagedConnection;

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

    @Override
    protected Properties applyProperties(Properties properties) {
        properties.put("datanucleus.rdbms.mysql.characterSet", "utf8mb3");
        properties.put("datanucleus.rdbms.mysql.collation", "utf8mb3_bin");
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
}
