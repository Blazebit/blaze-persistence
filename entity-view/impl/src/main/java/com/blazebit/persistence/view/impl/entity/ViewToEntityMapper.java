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
public interface ViewToEntityMapper extends ElementToEntityMapper {

    public FetchGraphNode<?> getFullGraphNode();

    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher);

    public AttributeAccessor getViewIdAccessor();

    public AttributeAccessor getEntityIdAccessor();

    public Query createUpdateQuery(UpdateContext context, Object view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode);

    public EntityViewUpdater getUpdater(Object view);

}