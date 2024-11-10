/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistListener;
import com.blazebit.persistence.view.ViewListener;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPostPersistListenerImpl<T> implements PostPersistListener<T> {

    private final ViewListener<T> listener;

    public ViewPostPersistListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, T view) {
        listener.call(view);
    }
}
