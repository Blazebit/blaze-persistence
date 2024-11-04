/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostUpdateListener;
import com.blazebit.persistence.view.ViewListener;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPostUpdateListenerImpl<T> implements PostUpdateListener<T> {

    private final ViewListener<T> listener;

    public ViewPostUpdateListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postUpdate(EntityViewManager entityViewManager, EntityManager entityManager, T view) {
        listener.call(view);
    }
}
