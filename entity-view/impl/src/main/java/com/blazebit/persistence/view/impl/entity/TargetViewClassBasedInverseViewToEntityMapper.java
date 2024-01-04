/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.metamodel.ViewTypeImplementor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.Query;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class TargetViewClassBasedInverseViewToEntityMapper<E> implements InverseElementToEntityMapper<E> {

    private final InverseViewToEntityMapper<E> first;
    private final Map<Class<?>, InverseViewToEntityMapper<E>> mappers;

    public TargetViewClassBasedInverseViewToEntityMapper(InverseViewToEntityMapper<E> first, Map<Class<?>, InverseViewToEntityMapper<E>> mappers) {
        this.first = first;
        this.mappers = mappers;
    }

    public ViewTypeImplementor<?> getViewType() {
        return first.getViewType();
    }

    @Override
    public void flushEntity(UpdateContext context, Object oldParent, Object newParent, Object child, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        InverseElementToEntityMapper<E> mapper = child == null ? first : mappers.get(((EntityViewProxy) child).$$_getEntityViewClass());
        mapper.flushEntity(context, oldParent, newParent, child, nestedGraphNode);
    }

    @Override
    public Query createInverseUpdateQuery(UpdateContext context, Object element, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        InverseElementToEntityMapper<E> mapper = element == null ? first : mappers.get(((EntityViewProxy) element).$$_getEntityViewClass());
        return mapper.createInverseUpdateQuery(context, element, nestedGraphNode, inverseAttributeFlusher);
    }
}
