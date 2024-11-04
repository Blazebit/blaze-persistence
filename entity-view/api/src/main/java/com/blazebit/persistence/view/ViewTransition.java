/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The transition types for entity views.
 *
 * @author Christian Beikov
 * @since 1.4.0
 * @see PostCommit
 * @see PostCommitListener
 * @see PostRollback
 * @see PostRollbackListener
 */
public enum ViewTransition {

    /**
     * The transition for entity views that were persisted.
     */
    PERSIST,
    /**
     * The transition for entity views that were updated.
     */
    UPDATE,
    /**
     * The transition for entity views that were removed.
     */
    REMOVE;
}
