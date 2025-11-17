/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistEntityListener;
import com.blazebit.persistence.view.ViewAndEntityListener;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewAndEntityPostPersistListenerImpl<T, E> implements PostPersistEntityListener<T, E> {

    private final ViewAndEntityListener<T, E> listener;

    public ViewAndEntityPostPersistListenerImpl(ViewAndEntityListener<T, E> listener) {
        this.listener = listener;
    }

    @Override
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, T view, E entity) {
        listener.call(view, entity);
    }
}
