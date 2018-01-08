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

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LoadOnlyViewToEntityMapper implements ViewToEntityMapper {
    protected final EntityLoader entityLoader;
    protected final AttributeAccessor viewIdAccessor;
    protected final AttributeAccessor entityIdAccessor;

    public LoadOnlyViewToEntityMapper(EntityLoader entityLoader, AttributeAccessor viewIdAccessor, AttributeAccessor entityIdAccessor) {
        this.entityLoader = entityLoader;
        this.viewIdAccessor = viewIdAccessor;
        this.entityIdAccessor = viewIdAccessor == null ? null : entityIdAccessor;
    }

    @Override
    public FetchGraphNode<?> getFullGraphNode() {
        return null;
    }

    @Override
    public EntityViewUpdater getUpdater(Object current) {
        return null;
    }

    @Override
    public void remove(UpdateContext context, Object element) {

    }

    @Override
    public void removeById(UpdateContext context, Object id) {

    }

    @Override
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher) {
        return fullFlusher;
    }

    @Override
    public Query createUpdateQuery(UpdateContext context, Object view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode) {
        return null;
    }

    @Override
    public Object applyToEntity(UpdateContext context, Object entity, Object view) {
        if (view == null) {
            return null;
        }

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(view);
        }
        return entityLoader.toEntity(context, id);
    }

    @Override
    public AttributeAccessor getViewIdAccessor() {
        return viewIdAccessor;
    }

    @Override
    public AttributeAccessor getEntityIdAccessor() {
        return entityIdAccessor;
    }
}
