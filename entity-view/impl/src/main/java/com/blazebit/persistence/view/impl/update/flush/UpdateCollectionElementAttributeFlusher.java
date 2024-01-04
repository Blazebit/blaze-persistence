/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

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
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
        Query q = null;
        if (viewToEntityMapper != null) {
            if (!nestedGraphNode.supportsQueryFlush() || context.isForceEntity()) {
                nestedGraphNode.flushEntity(context, null, element, (V) element, (V) element, null);
            } else {
                q = nestedGraphNode.flushQuery(context, parameterPrefix, viewToEntityMapper, q, element, null, (V) element, ownerAwareDeleter, nestedGraphNode);
            }
        } else {
            q = nestedGraphNode.flushQuery(context, parameterPrefix, viewToEntityMapper, q, element, null, (V) element, ownerAwareDeleter, nestedGraphNode);
        }
        if (q != null) {
            int updated = q.executeUpdate();

            if (updated != 1) {
                throw new OptimisticLockException("The update operation did not return the expected update count!", null, element);
            }
        }
        context.removeOrphans(orphanRemovalStartIndex);
        return query;
    }
}
