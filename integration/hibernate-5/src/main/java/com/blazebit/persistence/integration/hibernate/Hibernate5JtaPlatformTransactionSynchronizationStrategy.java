/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class Hibernate5JtaPlatformTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {

    private final JtaPlatform jtaPlatform;
    private final TransactionManager jtaTransactionManager;

    public Hibernate5JtaPlatformTransactionSynchronizationStrategy(JtaPlatform jtaPlatform) {
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