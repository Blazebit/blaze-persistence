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

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class FusedCollectionElementActions implements FusedCollectionActions {

    private final ViewToEntityMapper loadOnlyViewToEntityMapper;
    private final Map<Object, Object> removed;
    private final Map<Object, Object> added;

    public FusedCollectionElementActions(ViewToEntityMapper loadOnlyViewToEntityMapper, Map<Object, Object> removed, Map<Object, Object> added) {
        this.loadOnlyViewToEntityMapper = loadOnlyViewToEntityMapper;
        this.removed = removed;
        this.added = added;
    }

    @Override
    public int operationCount() {
        return removed.size() + added.size();
    }

    @Override
    public int getRemoveCount() {
        return removed.size();
    }

    @Override
    public int getAddCount() {
        return added.size();
    }

    @Override
    public int getUpdateCount() {
        return 0;
    }

    @Override
    public Collection<Object> getAdded() {
        return added.values();
    }

    @Override
    public Collection<Object> getAdded(UpdateContext context) {
        if (loadOnlyViewToEntityMapper == null) {
            return added.values();
        } else {
            return getEntityReferencesForCollectionOperation(context, added.values());
        }
    }

    public Collection<Object> getRemoved() {
        return removed.values();
    }

    public Collection<Object> getRemoved(UpdateContext context) {
        if (loadOnlyViewToEntityMapper == null) {
            return removed.values();
        } else {
            return getEntityReferencesForCollectionOperation(context, removed.values());
        }
    }

    private List<Object> getEntityReferencesForCollectionOperation(UpdateContext context, Collection<Object> objects) {
        List<Object> entityReferences = new ArrayList<>(objects.size());
        for (Object o : objects) {
            if (o != null) {
                entityReferences.add(o);
            }
        }
        loadOnlyViewToEntityMapper.applyAll(context, entityReferences);
        return entityReferences;
    }
}
