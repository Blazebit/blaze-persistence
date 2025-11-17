/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import jakarta.transaction.Synchronization;

/**
 * A transaction control abstraction that allows to be independent of the concrete transaction technology.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface TransactionAccess {

    /**
     * Returns whether the current transaction is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive();

    /**
     * Returns the current transaction object.
     *
     * @return the current transaction object
     */
    public Object getTransaction();

    /**
     * Mark the current transaction as rollback only.
     */
    public void markRollbackOnly();

    /**
     * Registers the given synchronization in the current transaction.
     *
     * @param synchronization The synchronization to register
     */
    public void registerSynchronization(Synchronization synchronization);
    
}
