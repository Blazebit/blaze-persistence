/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.EntityViewBuilderListener;
import com.blazebit.persistence.view.EntityViewNestedBuilder;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EntityViewNestedBuilderImpl<ViewType, ResultType> extends EntityViewBuilderBaseImpl<ViewType, EntityViewNestedBuilderImpl<ViewType, ResultType>> implements EntityViewNestedBuilder<ViewType, ResultType, EntityViewNestedBuilderImpl<ViewType, ResultType>> {

    private final ResultType result;
    private final EntityViewBuilderListener listener;

    public EntityViewNestedBuilderImpl(EntityViewManagerImpl evm, ManagedViewTypeImplementor<ViewType> managedViewType, Map<ManagedViewType<? extends ViewType>, String> inheritanceSubtypeMappings, Map<String, Object> optionalParameters, ResultType result, EntityViewBuilderListener listener) {
        super(evm, managedViewType, managedViewType.getDefaultConstructor(), inheritanceSubtypeMappings, optionalParameters);
        this.result = result;
        this.listener = listener;
    }

    @Override
    public ResultType build() {
        listener.onBuildComplete(buildObject());
        return result;
    }
}