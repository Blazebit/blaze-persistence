/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import jakarta.persistence.EntityManager;

/**
 * A listener for getting a callback before a view is removed.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PreRemoveListener<T> {

    /**
     * A callback that is invoked before the given view is removed.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that is about to be removed
     * @return <code>true</code> if the remove operation should be done, <code>false</code> if it should be cancelled
     */
    public boolean preRemove(EntityViewManager entityViewManager, EntityManager entityManager, T view);
}
