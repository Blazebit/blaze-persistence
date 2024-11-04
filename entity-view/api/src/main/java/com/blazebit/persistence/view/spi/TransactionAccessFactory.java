/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import javax.persistence.EntityManager;

/**
 * A factory for creating a {@link TransactionAccess}.
 * This is created via the {@link java.util.ServiceLoader} API.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface TransactionAccessFactory {

    /**
     * Creates a transaction access object.
     *
     * @param entityManager The entity manager associated with the transaction.
     * @return The transaction access object
     */
    TransactionAccess createTransactionAccess(EntityManager entityManager);

    /**
     * Returns a priority value that is used to select among multiple implementations.
     * The lower the returned value, the higher the priority.
     *
     * @return the priority value
     */
    int getPriority();
}
