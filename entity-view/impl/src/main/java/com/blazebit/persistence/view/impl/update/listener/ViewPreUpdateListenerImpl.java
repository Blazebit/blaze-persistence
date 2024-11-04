/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PreUpdateListener;
import com.blazebit.persistence.view.ViewListener;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPreUpdateListenerImpl<T> implements PreUpdateListener<T> {

    private final ViewListener<T> listener;

    public ViewPreUpdateListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void preUpdate(EntityViewManager entityViewManager, EntityManager entityManager, T view) {
        listener.call(view);
    }
}
