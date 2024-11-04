/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;

/**
 * A listener for getting a callback after persisting a creatable view.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PostPersistListener<T> {

    /**
     * A callback that is invoked after the given view is persisted.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that was persisted
     */
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, T view);
}
