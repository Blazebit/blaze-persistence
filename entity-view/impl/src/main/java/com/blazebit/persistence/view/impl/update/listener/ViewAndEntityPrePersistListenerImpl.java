/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PrePersistEntityListener;
import com.blazebit.persistence.view.ViewAndEntityListener;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewAndEntityPrePersistListenerImpl<T, E> implements PrePersistEntityListener<T, E> {

    private final ViewAndEntityListener<T, E> listener;

    public ViewAndEntityPrePersistListenerImpl(ViewAndEntityListener<T, E> listener) {
        this.listener = listener;
    }

    @Override
    public void prePersist(EntityViewManager entityViewManager, EntityManager entityManager, T view, E entity) {
        listener.call(view, entity);
    }
}
