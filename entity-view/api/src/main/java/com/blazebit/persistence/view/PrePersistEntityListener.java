/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;

/**
 * A listener for getting a callback before persisting a creatable view.
 *
 * @param <T> The view type
 * @param <E> The entity type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface PrePersistEntityListener<T, E> {

    /**
     * A callback that is invoked before the given view is persisted.
     *
     * @param entityViewManager The entity view manager
     * @param entityManager The entity manager
     * @param view The view that is about to be persisted
     * @param entity The entity object that is about to be persisted
     */
    public void prePersist(EntityViewManager entityViewManager, EntityManager entityManager, T view, E entity);
}
