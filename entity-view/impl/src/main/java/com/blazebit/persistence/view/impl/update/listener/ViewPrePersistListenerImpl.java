/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PrePersistListener;
import com.blazebit.persistence.view.ViewListener;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPrePersistListenerImpl<T> implements PrePersistListener<T> {

    private final ViewListener<T> listener;

    public ViewPrePersistListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void prePersist(EntityViewManager entityViewManager, EntityManager entityManager, T view) {
        listener.call(view);
    }
}
