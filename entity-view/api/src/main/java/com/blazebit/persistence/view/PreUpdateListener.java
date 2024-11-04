/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;

/**
 * A listener for getting a callback before updating an updatable view.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PreUpdateListener<T> {

    /**
     * A callback that is invoked before the given view is updated.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that is about to be updated
     */
    public void preUpdate(EntityViewManager entityViewManager, EntityManager entityManager, T view);
}
