/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The lock mode types for updatable entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum LockMode {

    /**
     * The automatic lock mode will use optimistic locking if possible or no locking.
     */
    AUTO,
    /**
     * The optimistic locking mode will use the version attribute of an entity to for optimistic locking.
     */
    OPTIMISTIC,
    /**
     * The pessimistic read locking mode will acquire a {@link jakarta.persistence.LockModeType#PESSIMISTIC_READ} for the entity when reading the entity view.
     * This lock mode is only useful within the bounds of a single transaction as the lock is bound to it.
     */
    PESSIMISTIC_READ,
    /**
     * The pessimistic write locking mode will acquire a {@link jakarta.persistence.LockModeType#PESSIMISTIC_WRITE} for the entity when reading the entity view.
     * This lock mode is only useful within the bounds of a single transaction as the lock is bound to it.
     */
    PESSIMISTIC_WRITE,
    /**
     * No locking at any point is done.
     */
    NONE;
}
