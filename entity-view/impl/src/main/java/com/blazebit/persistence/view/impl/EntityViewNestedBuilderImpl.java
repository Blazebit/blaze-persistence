/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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