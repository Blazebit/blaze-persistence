/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PrePersistEntityListener;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePrePersistEntityListener extends ViewInstanceEntityListener implements PrePersistEntityListener<Object, Object> {

    public ViewInstancePrePersistEntityListener(Method listener) {
        super(listener);
    }

    @Override
    public void prePersist(EntityViewManager entityViewManager, EntityManager entityManager, Object view, Object entity) {
        invoke(entityViewManager, entityManager, view, entity);
    }
}
