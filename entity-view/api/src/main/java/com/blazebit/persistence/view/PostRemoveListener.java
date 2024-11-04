/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;

/**
 * A listener for getting a callback after a view is removed.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PostRemoveListener<T> {

    /**
     * A callback that is invoked after the given view is removed.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that was removed
     */
    public void postRemove(EntityViewManager entityViewManager, EntityManager entityManager, T view);
}
