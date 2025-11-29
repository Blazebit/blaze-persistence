/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostUpdateListener;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePostUpdateListener extends ViewInstanceListener implements PostUpdateListener<Object> {

    public ViewInstancePostUpdateListener(Method listener) {
        super(listener);
    }

    @Override
    public void postUpdate(EntityViewManager entityViewManager, EntityManager entityManager, Object view) {
        invoke(entityViewManager, entityManager, view);
    }
}
