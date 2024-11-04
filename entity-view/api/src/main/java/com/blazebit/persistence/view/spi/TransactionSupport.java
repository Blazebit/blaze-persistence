/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

/**
 * A transaction control abstraction that allows to be independent of the concrete transaction technology.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface TransactionSupport {

    /**
     * Starts a new transaction, possibly suspending an existing one, and runs the given runnable within.
     *
     * @param runnable The runnable to run in a new transaction
     */
    public void transactional(Runnable runnable);
    
}
