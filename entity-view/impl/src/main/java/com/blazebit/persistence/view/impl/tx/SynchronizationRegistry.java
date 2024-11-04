/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.persistence.view.spi.TransactionAccess;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The main purpose of a custom registry is to invoke synchronizations in reverse order when rolling back.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SynchronizationRegistry implements Synchronization, TransactionAccess {

    // We don't use a thread local because a TX could be rolled back from a different thread
    private static final ConcurrentMap<Thread, List<SynchronizationRegistry>> REGISTRY = new ConcurrentHashMap<>();
    private final Object transaction;
    private final TransactionAccess transactionAccess;
    private final List<Synchronization> synchronizations;
    private final Thread key;

    public SynchronizationRegistry(TransactionAccess transactionAccess) {
        this.transactionAccess = transactionAccess;
        this.transaction = transactionAccess.getTransaction();
        this.synchronizations = new ArrayList<>(1);
        this.key = Thread.currentThread();
        transactionAccess.registerSynchronization(this);
        List<SynchronizationRegistry> synchronizationRegistries = new ArrayList<>(1);
        List<SynchronizationRegistry> existingSynchronizationRegistries = REGISTRY.putIfAbsent(key, synchronizationRegistries);
        if (existingSynchronizationRegistries != null) {
            synchronizationRegistries = existingSynchronizationRegistries;
        }
        synchronizationRegistries.add(SynchronizationRegistry.this);
    }

    public static SynchronizationRegistry getRegistry() {
        List<SynchronizationRegistry> synchronizationRegistries = REGISTRY.get(Thread.currentThread());
        if (synchronizationRegistries != null) {
            for (SynchronizationRegistry registry : synchronizationRegistries) {
                if (registry.transactionAccess.getTransaction() == registry.getTransaction()) {
                    return registry;
                }
            }
        }
        return null;
    }

    public TransactionAccess getTransactionAccess() {
        return transactionAccess;
    }

    @Override
    public Object getTransaction() {
        return transaction;
    }

    @Override
    public boolean isActive() {
        return transactionAccess.isActive();
    }

    @Override
    public void markRollbackOnly() {
        transactionAccess.markRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }

    @Override
    public void beforeCompletion() {
        List<Exception> suppressedExceptions = null;
        for (int i = 0; i < synchronizations.size(); i++) {
            Synchronization synchronization = synchronizations.get(i);
            try {
                synchronization.beforeCompletion();
            } catch (Exception ex) {
                if (suppressedExceptions == null) {
                    suppressedExceptions = new ArrayList<>();
                }
                suppressedExceptions.add(ex);
            }
        }
        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during beforeCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during beforeCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }

    @Override
    public void afterCompletion(int status) {
        List<Exception> suppressedExceptions = null;
        List<SynchronizationRegistry> synchronizationRegistries;
        switch (status) {
            // We don't care about these statuses, only about committed and rolled back
            case Status.STATUS_ACTIVE:
            case Status.STATUS_COMMITTING:
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_NO_TRANSACTION:
            case Status.STATUS_PREPARED:
            case Status.STATUS_PREPARING:
                break;
            case Status.STATUS_COMMITTED:
                synchronizationRegistries = REGISTRY.get(key);
                if (synchronizationRegistries != null) {
                    synchronizationRegistries.remove(this);
                    if (synchronizationRegistries.isEmpty()) {
                        REGISTRY.remove(key);
                    }
                }
                for (int i = 0; i < synchronizations.size(); i++) {
                    Synchronization synchronization = synchronizations.get(i);
                    try {
                        synchronization.afterCompletion(status);
                    } catch (Exception ex) {
                        if (suppressedExceptions == null) {
                            suppressedExceptions = new ArrayList<>();
                        }
                        suppressedExceptions.add(ex);
                    }
                }
                break;
            case Status.STATUS_ROLLING_BACK:
            case Status.STATUS_ROLLEDBACK:
            // We assume unknown means rolled back as Hibernate behaves this way with a local transaction coordinator
            case Status.STATUS_UNKNOWN:
            default:
                synchronizationRegistries = REGISTRY.get(key);
                if (synchronizationRegistries != null) {
                    synchronizationRegistries.remove(this);
                    if (synchronizationRegistries.isEmpty()) {
                        REGISTRY.remove(key);
                    }
                }
                if (synchronizationRegistries != null) {
                    for (int i = synchronizations.size() - 1; i >= 0; i--) {
                        Synchronization synchronization = synchronizations.get(i);
                        try {
                            synchronization.afterCompletion(status);
                        } catch (Exception ex) {
                            if (suppressedExceptions == null) {
                                suppressedExceptions = new ArrayList<>();
                            }
                            suppressedExceptions.add(ex);
                        }
                    }
                }
                break;
        }

        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during afterCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during afterCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }
}
