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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewUpdater {

    public FetchGraphNode<?> getFullGraphNode();

    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher);

    public void executeUpdate(UpdateContext context, MutableStateTrackable updatableProxy);

    public Object executeUpdate(UpdateContext context, Object entity, MutableStateTrackable updatableProxy);

    public Object executePersist(UpdateContext context, MutableStateTrackable updatableProxy);

    public Object executePersist(UpdateContext context, Object entity, MutableStateTrackable updatableProxy);

    public void remove(UpdateContext context, EntityViewProxy entityView);

    public void remove(UpdateContext context, Object id);

    public Query createUpdateQuery(UpdateContext context, MutableStateTrackable view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode);

    public DirtyChecker<DirtyStateTrackable> getDirtyChecker();
}
