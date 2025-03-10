/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PrePersistListener;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePrePersistListener extends ViewInstanceListener implements PrePersistListener<Object> {

    public ViewInstancePrePersistListener(Method listener) {
        super(listener);
    }

    @Override
    public void prePersist(EntityViewManager entityViewManager, EntityManager entityManager, Object view) {
        invoke(entityViewManager, entityManager, view);
    }
}
