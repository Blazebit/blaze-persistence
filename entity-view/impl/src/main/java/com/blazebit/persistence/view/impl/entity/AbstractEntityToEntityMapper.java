/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.UnmappedAttributeCascadeDeleter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityToEntityMapper implements EntityToEntityMapper {

    protected final EntityLoaderFetchGraphNode<?> entityLoaderFetchGraphNode;
    private final UnmappedAttributeCascadeDeleter deleter;

    public AbstractEntityToEntityMapper(EntityLoaderFetchGraphNode<?> entityLoaderFetchGraphNode, UnmappedAttributeCascadeDeleter deleter) {
        this.entityLoaderFetchGraphNode = entityLoaderFetchGraphNode;
        this.deleter = deleter;
    }

    @Override
    public void remove(UpdateContext context, Object element) {
        context.getEntityManager().remove(element);
    }

    @Override
    public void removeById(UpdateContext context, Object elementId) {
        deleter.removeById(context, elementId);
    }

    @Override
    public EntityLoaderFetchGraphNode<?> getFullGraphNode() {
        return entityLoaderFetchGraphNode;
    }

    @Override
    public EntityLoaderFetchGraphNode<?> getFetchGraph(String[] dirtyProperties) {
        // TODO: extract the relevant fetch graph parts from the dirty properties
        return entityLoaderFetchGraphNode;
    }
}
