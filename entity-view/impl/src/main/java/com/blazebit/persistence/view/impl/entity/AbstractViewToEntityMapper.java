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

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.EntityIdAttributeAccessor;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.proxy.EntityViewProxy;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.Type;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractViewToEntityMapper implements ViewToEntityMapper {

    protected final String attributeLocation;
    protected final Class<?> viewTypeClass;
    protected final boolean isEmbeddable;
    protected final boolean isFlatView;
    protected final Map<Class<?>, EntityViewUpdater> persistUpdater;
    protected final Map<Class<?>, EntityViewUpdater> updateUpdater;
    protected final FetchGraphNode<?> fullGraphNode;
    protected final EntityLoader entityLoader;
    protected final AttributeAccessor viewIdAccessor;
    protected final boolean persistAllowed;

    public AbstractViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Class<?> viewTypeClass, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes, EntityLoader entityLoader, AttributeAccessor viewIdAccessor, boolean persistAllowed) {
        this.attributeLocation = attributeLocation;
        this.viewTypeClass = viewTypeClass;
        this.isEmbeddable = evm.getMetamodel().getEntityMetamodel().getEntity(evm.getMetamodel().managedView(viewTypeClass).getEntityClass()) == null;
        this.isFlatView = evm.getMetamodel().managedView(viewTypeClass) instanceof FlatViewType<?>;
        Map<Class<?>, EntityViewUpdater> persistUpdater = new HashMap<>();
        Map<Class<?>, EntityViewUpdater> updateUpdater = new HashMap<>();

        for (Type<?> t : persistAllowedSubtypes) {
            persistUpdater.put(t.getJavaType(), evm.getUpdater((ManagedViewTypeImpl<?>) t));
        }
        for (Type<?> t : updateAllowedSubtypes) {
            updateUpdater.put(t.getJavaType(), evm.getUpdater((ManagedViewTypeImpl<?>) t));
        }

        this.persistUpdater = Collections.unmodifiableMap(persistUpdater);
        this.updateUpdater = Collections.unmodifiableMap(updateUpdater);
        this.fullGraphNode = computeFullGraphNode();
        this.entityLoader = entityLoader;
        this.viewIdAccessor = viewIdAccessor;
        this.persistAllowed = persistAllowed;
    }

    @SuppressWarnings("unchecked")
    private FetchGraphNode<?> computeFullGraphNode() {
        final int size = updateUpdater.size();
        switch (size) {
            case 0: return null;
            case 1: return updateUpdater.values().iterator().next().getFullGraphNode();
            default: break;
        }

        Iterator<EntityViewUpdater> iter = updateUpdater.values().iterator();
        FetchGraphNode<?> firstGraph = iter.next().getFullGraphNode();
        List<FetchGraphNode<?>> graphNodes = new ArrayList<>(size);
        graphNodes.add(firstGraph);

        do {
            graphNodes.add(iter.next().getFullGraphNode());
        } while (iter.hasNext());

        return firstGraph.mergeWith((List) graphNodes);
    }

    @Override
    public FetchGraphNode<?> getFullGraphNode() {
        return fullGraphNode;
    }

    @Override
    public EntityViewUpdater getUpdater(Object current) {
        Class<?> viewTypeClass = getViewTypeClass(current);

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(null, current);
        }

        if (shouldPersist(current, id)) {
            if (!persistAllowed) {
                return null;
            }
            return persistUpdater.get(viewTypeClass);
        }

        return null;
    }

    @Override
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher) {
        if (current == null) {
            return fullFlusher;
        }

        Object id = null;
        if (viewIdAccessor != null) {
            id = viewIdAccessor.getValue(context, current);
        }
        Class<?> viewTypeClass = getViewTypeClass(current);

        if (shouldPersist(current, id)) {
            if (!persistAllowed) {
                return null;
            }
            EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
            if (updater == null) {
                return null;
            }

            return updater.getNestedDirtyFlusher(context, current, fullFlusher);
        }

        return null;
    }

    @Override
    public Query createUpdateQuery(UpdateContext context, Object view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode) {
        return null;
    }

    protected Object persist(UpdateContext context, Object entity, Object view) {
        if (persistAllowed) {
            Class<?> viewTypeClass = getViewTypeClass(view);
            EntityViewUpdater updater = persistUpdater.get(viewTypeClass);
            if (updater == null) {
                throw new IllegalStateException("Couldn't persist object for " + attributeLocation + ". Expected subviews of the types " + persistUpdater.keySet() + " but got: " + view);
            }
            if (entity != null) {
                return updater.executePersist(context, entity, (MutableStateTrackable) view);
            } else {
                return updater.executePersist(context, (MutableStateTrackable) view);
            }
        }

        return entity;
    }

    protected boolean shouldPersist(Object view, Object id) {
        // Flat view types are always considered to be "persisted"
        if (isFlatView) {
            return true;
        }

        // We assume if the view has no id set, it will be generated on persist. If it isn't JPA will complain appropriately
        // If it has an id, it could still need persisting which we detect if it was created via EntityViewManager.create()
        return id == null || (view instanceof EntityViewProxy && ((EntityViewProxy) view).$$_isNew());
    }

    protected Class<?> getViewTypeClass(Object view) {
        if (view instanceof EntityViewProxy) {
            return ((EntityViewProxy) view).$$_getEntityViewClass();
        }

        return view.getClass();
    }

    @Override
    public AttributeAccessor getViewIdAccessor() {
        return viewIdAccessor;
    }

    @Override
    public AttributeAccessor getEntityIdAccessor() {
        return viewIdAccessor == null ? null : EntityIdAttributeAccessor.INSTANCE;
    }

}
