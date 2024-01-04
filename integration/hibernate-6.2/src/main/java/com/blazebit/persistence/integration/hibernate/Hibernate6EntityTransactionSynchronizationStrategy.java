/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.resource.transaction.spi.SynchronizationRegistry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Synchronization;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class Hibernate6EntityTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {
    
    private final EntityTransaction tx;
    private final SynchronizationRegistry synchronizationRegistry;

    public Hibernate6EntityTransactionSynchronizationStrategy(EntityTransaction tx, EntityManager em) {
        this.tx = tx;
        Session s = em.unwrap(Session.class);
        this.synchronizationRegistry = ((SessionImplementor) s).getTransactionCoordinator().getLocalSynchronizations();
    }

    @Override
    public boolean isActive() {
        return tx.isActive();
    }

    @Override
    public void markRollbackOnly() {
        tx.setRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizationRegistry.registerSynchronization(synchronization);
    }

    @Override
    public void transactional(Runnable runnable) {
        // In resource local mode, we have no global transaction state
        runnable.run();
    }
}
