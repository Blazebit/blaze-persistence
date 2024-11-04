/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class ViewIdAttributeAccessor extends ViewAttributeAccessor {

    ViewIdAttributeAccessor(EntityViewManagerImpl evm, ViewType<?> viewType, boolean readonly) {
        super(evm, viewType.getIdAttribute(), readonly);
    }

    @Override
    public Object getValue(Object view) {
        if (view instanceof EntityViewProxy) {
            EntityViewProxy proxy = (EntityViewProxy) view;
            return proxy.$$_getId();
        }
        return super.getValue(view);
    }
}
