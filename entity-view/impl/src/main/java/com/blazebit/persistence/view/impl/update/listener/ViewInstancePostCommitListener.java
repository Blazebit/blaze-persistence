/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.ViewTransition;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePostCommitListener extends ViewInstanceTransitionListener implements PostCommitListener<Object> {

    public ViewInstancePostCommitListener(Method listener) {
        super(listener);
    }

    @Override
    public void postCommit(EntityViewManager entityViewManager, EntityManager entityManager, Object view, ViewTransition transition) {
        invoke(entityViewManager, entityManager, view, transition);
    }
}
