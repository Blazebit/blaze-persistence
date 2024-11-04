/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.exception.ExceptionUtils;
import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JtaTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {

    private final TransactionManager tm;
    private final TransactionSynchronizationRegistry synchronizationRegistry;

    public JtaTransactionSynchronizationStrategy(JtaResources jtaResources) {
        this(jtaResources.getTransactionManager(), jtaResources.getTransactionSynchronizationRegistry());
    }

    public JtaTransactionSynchronizationStrategy(TransactionManager tm, TransactionSynchronizationRegistry synchronizationRegistry) {
        this.tm = tm;
        this.synchronizationRegistry = synchronizationRegistry;
    }

    @Override
    public boolean isActive() {
        return synchronizationRegistry.getTransactionStatus() == Status.STATUS_ACTIVE;
    }

    @Override
    public Object getTransaction() {
        try {
            return tm.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markRollbackOnly() {
        synchronizationRegistry.setRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizationRegistry.registerInterposedSynchronization(synchronization);
    }

    @Override
    public void transactional(Runnable runnable) {
        Transaction tx = null;
        Transaction suspendedTx = null;
        Throwable exception = null;
        try {
            int status = tm.getStatus();
            if (status == Status.STATUS_NO_TRANSACTION) {
                tm.begin();
                tx = tm.getTransaction();
            } else {
                suspendedTx = tm.suspend();
                tm.begin();
                tx = tm.getTransaction();
            }

            runnable.run();
        } catch (Throwable t) {
            exception = t;
            if (tx != null) {
                try {
                    tx.setRollbackOnly();
                } catch (Throwable e) {
                    t.addSuppressed(e);
                }
            }
            ExceptionUtils.doThrow(t);
        } finally {
            try {
                if (tx != null) {
                    switch (tx.getStatus()) {
                        case Status.STATUS_ACTIVE:
                        case Status.STATUS_PREPARED:
                        case Status.STATUS_COMMITTED:
                        case Status.STATUS_UNKNOWN:
                        case Status.STATUS_NO_TRANSACTION:
                        case Status.STATUS_PREPARING:
                        case Status.STATUS_COMMITTING:
                            tm.commit();
                            break;
                        case Status.STATUS_MARKED_ROLLBACK:
                        case Status.STATUS_ROLLEDBACK:
                        case Status.STATUS_ROLLING_BACK:
                        default:
                            tm.rollback();
                            break;
                    }
                }
            } catch (Throwable e) {
                // Only handle errors when we didn't already had an error before
                if (exception != null) {
                    exception.addSuppressed(e);
                }
            } finally {
                if (suspendedTx != null) {
                    try {
                        tm.resume(suspendedTx);
                    } catch (Throwable e) {
                        if (exception != null) {
                            exception.addSuppressed(e);
                        }
                    }
                }
            }
        }
    }

}
