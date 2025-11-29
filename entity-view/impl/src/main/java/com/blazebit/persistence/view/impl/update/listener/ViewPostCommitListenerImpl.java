/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.ViewListener;
import com.blazebit.persistence.view.ViewTransition;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewPostCommitListenerImpl<T> implements PostCommitListener<T> {

    private final ViewListener<T> listener;

    public ViewPostCommitListenerImpl(ViewListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postCommit(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition) {
        listener.call(view);
    }
}
