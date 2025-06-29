/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public class Hibernate7EntityTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {
    
    private final EntityTransaction tx;
    private final SynchronizationRegistry synchronizationRegistry;

    public Hibernate7EntityTransactionSynchronizationStrategy(EntityTransaction tx, EntityManager em) {
        this.tx = tx;
        Session s = em.unwrap(Session.class);
        this.synchronizationRegistry = ((SessionImplementor) s).getTransactionCoordinator().getLocalSynchronizations();
    }

    @Override
    public boolean isActive() {
        return tx.isActive();
    }

    @Override
    public Object getTransaction() {
        return tx;
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
