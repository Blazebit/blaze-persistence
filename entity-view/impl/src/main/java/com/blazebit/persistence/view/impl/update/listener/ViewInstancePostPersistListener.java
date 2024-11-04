/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistListener;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePostPersistListener extends ViewInstanceListener implements PostPersistListener<Object> {
    
    public ViewInstancePostPersistListener(Method listener) {
        super(listener);
    }

    @Override
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, Object view) {
        invoke(entityViewManager, entityManager, view);
    }
}
