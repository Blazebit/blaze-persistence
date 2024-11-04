/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PreRemoveListener;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewInstancePreRemoveListener extends ViewInstanceListener implements PreRemoveListener<Object> {

    private final boolean possiblyCancelling;

    public ViewInstancePreRemoveListener(Method listener) {
        super(listener);
        this.possiblyCancelling = listener.getReturnType() == boolean.class || listener.getReturnType() == Boolean.class;
    }

    public boolean isPossiblyCancelling() {
        return possiblyCancelling;
    }

    @Override
    public boolean preRemove(EntityViewManager entityViewManager, EntityManager entityManager, Object view) {
        Object result = invoke(entityViewManager, entityManager, view);
        return !Boolean.FALSE.equals(result);
    }

}
