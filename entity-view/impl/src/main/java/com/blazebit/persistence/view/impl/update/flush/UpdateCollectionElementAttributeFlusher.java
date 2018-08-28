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

import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UpdateCollectionElementAttributeFlusher<E, V> extends CollectionElementAttributeFlusher<E, V> {

    protected final ViewToEntityMapper viewToEntityMapper;

    public UpdateCollectionElementAttributeFlusher(DirtyAttributeFlusher<?, E, V> nestedGraphNode, Object element, boolean optimisticLockProtected, ViewToEntityMapper viewToEntityMapper) {
        super(nestedGraphNode, element, optimisticLockProtected);
        this.viewToEntityMapper = viewToEntityMapper;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
        Query q = null;
        if (viewToEntityMapper != null) {
            if (!nestedGraphNode.supportsQueryFlush()) {
                nestedGraphNode.flushEntity(context, null, (V) element, (V) element, null);
            } else {
                q = viewToEntityMapper.createUpdateQuery(context, element, nestedGraphNode);
                nestedGraphNode.flushQuery(context, parameterPrefix, q, null, (V) element, ownerAwareDeleter);
            }
        } else {
            q = viewToEntityMapper.createUpdateQuery(context, element, nestedGraphNode);
            nestedGraphNode.flushQuery(context, parameterPrefix, q, null, (V) element, ownerAwareDeleter);
        }
        if (q != null) {
            int updated = q.executeUpdate();

            if (updated != 1) {
                throw new OptimisticLockException(null, element);
            }
        }
        context.removeOrphans(orphanRemovalStartIndex);
    }
}
