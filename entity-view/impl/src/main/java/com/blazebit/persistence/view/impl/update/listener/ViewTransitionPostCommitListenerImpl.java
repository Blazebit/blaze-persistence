/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.ViewTransitionListener;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewTransitionPostCommitListenerImpl<T> implements PostCommitListener<T> {

    private final ViewTransitionListener<T> listener;

    public ViewTransitionPostCommitListenerImpl(ViewTransitionListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postCommit(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition) {
        listener.call(view, transition);
    }
}
