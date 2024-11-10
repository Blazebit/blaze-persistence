/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import jakarta.persistence.EntityManager;

/**
 * A listener for getting a callback after updating an updatable view.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PostUpdateListener<T> {

    /**
     * A callback that is invoked after the given view is updated.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that was updated
     */
    public void postUpdate(EntityViewManager entityViewManager, EntityManager entityManager, T view);
}
