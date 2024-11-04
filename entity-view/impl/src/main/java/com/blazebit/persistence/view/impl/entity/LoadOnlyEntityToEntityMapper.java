/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.UnmappedAttributeCascadeDeleter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LoadOnlyEntityToEntityMapper extends AbstractEntityToEntityMapper {

    public LoadOnlyEntityToEntityMapper(EntityLoaderFetchGraphNode<?> entityLoaderFetchGraphNode, UnmappedAttributeCascadeDeleter deleter) {
        super(entityLoaderFetchGraphNode, deleter);
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object dirtyEntity) {
        if (dirtyEntity == null) {
            return null;
        }

        Object id = entityLoaderFetchGraphNode.getEntityId(context, dirtyEntity);
        return entityLoaderFetchGraphNode.toEntity(context, null, id);
    }

    @Override
    public DirtyChecker<?> getDirtyChecker() {
        return null;
    }
}
