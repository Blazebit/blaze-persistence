/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.EntityViewBuilder;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EntityViewBuilderImpl<T> extends EntityViewBuilderBaseImpl<T, EntityViewBuilder<T>> implements EntityViewBuilder<T> {

    public EntityViewBuilderImpl(EntityViewManagerImpl evm, ManagedViewTypeImplementor<T> managedViewType, MappingConstructorImpl<T> mappingConstructor, Map<String, Object> optionalParameters) {
        super(evm, managedViewType, mappingConstructor, null, optionalParameters);
    }

    @Override
    public T build() {
        return buildObject();
    }

}