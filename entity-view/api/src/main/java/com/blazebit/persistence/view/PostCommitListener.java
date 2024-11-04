/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import jakarta.persistence.EntityManager;

/**
 * A listener for getting a callback after committing a flush for an entity view.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PostCommitListener<T> {

    /**
     * A callback that is invoked after the flush for given view was committed.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view for which the flush committed
     * @param transition The view transition
     */
    public void postCommit(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition);
}
