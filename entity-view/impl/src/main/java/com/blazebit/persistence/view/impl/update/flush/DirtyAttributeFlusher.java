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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DirtyAttributeFlusher<T extends DirtyAttributeFlusher<T, E, V>, E, V> extends FetchGraphNode<T>, DirtyChecker<V> {

    public DirtyAttributeFlusher<T, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current);

    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator);

    public void appendFetchJoinQueryFragment(String base, StringBuilder sb);

    public boolean supportsQueryFlush();

    public boolean loadForEntityFlush();

    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue);
    
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter);

    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener);

    public void removeFromEntity(UpdateContext context, E entity);

    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value);

    public void remove(UpdateContext context, Object id);

    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id);

    public V cloneDeep(Object view, V oldValue, V newValue);

    public boolean isPassThrough();

    public String getElementIdAttributeName();

    public AttributeAccessor getViewAttributeAccessor();

    public boolean isOptimisticLockProtected();

    public boolean requiresFlushAfterPersist(V value);

    public boolean requiresDeleteCascadeAfterRemove();

    public boolean isViewOnlyDeleteCascaded();
}
