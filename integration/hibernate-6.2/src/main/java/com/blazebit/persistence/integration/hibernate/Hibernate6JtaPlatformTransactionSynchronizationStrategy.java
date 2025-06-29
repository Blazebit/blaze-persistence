/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

/**
 * @author Moritz Becker
 * @since 1.6.7
 */
public class Hibernate6JtaPlatformTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {

    private final JtaPlatform jtaPlatform;
    private final TransactionManager jtaTransactionManager;

    public Hibernate6JtaPlatformTransactionSynchronizationStrategy(JtaPlatform jtaPlatform) {
        this.jtaPlatform = jtaPlatform;
        this.jtaTransactionManager = jtaPlatform.retrieveTransactionManager();
    }

    @Override
    public boolean isActive() {
        try {
            return jtaPlatform.getCurrentStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getTransaction() {
        try {
            return jtaTransactionManager.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markRollbackOnly() {
        try {
            jtaTransactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        jtaPlatform.registerSynchronization(synchronization);
    }

    @Override
    public void transactional(Runnable runnable) {
        // In resource local mode, we have no global transaction state
        runnable.run();
    }

}