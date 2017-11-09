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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PersistCollectionElementAttributeFlusher<E, V> extends CollectionElementAttributeFlusher<E, V> {

    public PersistCollectionElementAttributeFlusher(Object element, boolean optimisticLockProtected) {
        super(null, element, optimisticLockProtected);
    }

    @Override
    public FetchGraphNode<?> mergeWith(List<CollectionElementAttributeFlusher<E, V>> fetchGraphNodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix) {
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value) {
        context.getEntityManager().persist(element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value) {
        context.getEntityManager().persist(element);
        return true;
    }

    @Override
    public void remove(UpdateContext context, E entity, Object view, V value) {
        // No-op
    }

    @Override
    public DirtyAttributeFlusher<CollectionElementAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        // Actually this should never be called, but let's return this to be safe
        return this;
    }
}
