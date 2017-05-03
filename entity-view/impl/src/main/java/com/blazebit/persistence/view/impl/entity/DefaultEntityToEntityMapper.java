/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.BasicDirtyChecker;
import com.blazebit.persistence.view.impl.update.flush.TypeDescriptor;
import com.blazebit.persistence.view.spi.BasicUserType;

import java.util.Collections;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultEntityToEntityMapper extends AbstractEntityToEntityMapper {

    private final boolean shouldPersist;
    private final boolean shouldMerge;
    private final BasicUserType<?> basicUserType;
    private final BasicDirtyChecker<Object> dirtyChecker;

    public DefaultEntityToEntityMapper(boolean shouldPersist, boolean shouldMerge, BasicUserType<?> basicUserType, EntityLoaderFetchGraphNode<?> entityLoaderFetchGraphNode) {
        super(entityLoaderFetchGraphNode);
        this.shouldPersist = shouldPersist;
        this.shouldMerge = shouldMerge;
        this.basicUserType = basicUserType;
        this.dirtyChecker = new BasicDirtyChecker<>(new TypeDescriptor(
                true,
                true,
                true,
                true,
                shouldMerge,
                shouldPersist,
                shouldPersist,
                shouldMerge,
                Collections.EMPTY_SET,
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
        Object loadedEntity = entityLoaderFetchGraphNode.toEntity(context, id);

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
