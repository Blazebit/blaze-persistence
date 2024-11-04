/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.BasicDirtyChecker;
import com.blazebit.persistence.view.impl.update.flush.TypeDescriptor;
import com.blazebit.persistence.view.impl.update.flush.UnmappedAttributeCascadeDeleter;
import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultEntityToEntityMapper extends AbstractEntityToEntityMapper {

    private final boolean shouldPersist;
    private final boolean shouldMerge;
    private final BasicUserType<Object> basicUserType;
    private final BasicDirtyChecker<Object> dirtyChecker;

    public DefaultEntityToEntityMapper(boolean shouldPersist, boolean shouldMerge, Class<?> jpaType, BasicUserType<?> basicUserType, EntityLoaderFetchGraphNode<?> entityLoaderFetchGraphNode, UnmappedAttributeCascadeDeleter deleter) {
        super(entityLoaderFetchGraphNode, deleter);
        this.shouldPersist = shouldPersist;
        this.shouldMerge = shouldMerge;
        this.basicUserType = (BasicUserType<Object>) basicUserType;
        this.dirtyChecker = new BasicDirtyChecker<>(new TypeDescriptor(
                true,
                true,
                true,
                true,
                shouldMerge,
                shouldPersist,
                shouldPersist,
                shouldMerge,
                null,
                null,
                jpaType,
                null,
                (BasicUserType<Object>) basicUserType,
                null,
                null,
                null
        ));
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object dirtyEntity) {
        if (dirtyEntity == null) {
            return null;
        }

        if (basicUserType.shouldPersist(dirtyEntity)) {
            if (shouldPersist) {
                context.getEntityManager().persist(dirtyEntity);
            }
        }

        Object id = entityLoaderFetchGraphNode.getEntityId(context, dirtyEntity);
        Object loadedEntity = entityLoaderFetchGraphNode.toEntity(context, null, id);

        if (shouldMerge) {
            return context.getEntityManager().merge(dirtyEntity);
        }

        return loadedEntity;
    }

    @Override
    public DirtyChecker<?> getDirtyChecker() {
        return dirtyChecker;
    }
}
