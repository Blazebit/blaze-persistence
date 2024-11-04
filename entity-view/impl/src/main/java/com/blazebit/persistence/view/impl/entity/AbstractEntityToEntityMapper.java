/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.UnmappedAttributeCascadeDeleter;

import java.util.List;

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

    @Override
    public void applyAll(UpdateContext context, List<Object> elements) {
        for (int i = 0; i < elements.size(); i++) {
            elements.set(i, applyToEntity(context, null, elements.get(i)));
        }
    }
}
