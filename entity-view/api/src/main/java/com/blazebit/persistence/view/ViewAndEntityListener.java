/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A listener for getting a callback.
 *
 * @param <T> The view type
 * @param <E> The entity type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface ViewAndEntityListener<T, E> {

    /**
     * A callback that is invoked for a view and entity.
     *
     * @param view The view that is about to be persisted
     * @param entity The entity object that is about to be persisted
     */
    public void call(T view, E entity);
}
