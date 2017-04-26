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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewMetamodelImpl implements ViewMetamodel {

    private final EntityMetamodel metamodel;
    private final Map<Class<?>, ViewTypeImpl<?>> views;
    private final Map<Class<?>, FlatViewTypeImpl<?>> flatViews;
    private final Map<Class<?>, ManagedViewTypeImpl<?>> managedViews;

    public ViewMetamodelImpl(Set<Class<?>> entityViews, ServiceProvider serviceProvider, MetamodelBuildingContext context, boolean validateExpressions) {
        this.metamodel = serviceProvider.getService(EntityMetamodel.class);

        Map<Class<?>, ViewTypeImpl<?>> views = new HashMap<>(entityViews.size());
        Map<Class<?>, FlatViewTypeImpl<?>> flatViews = new HashMap<>(entityViews.size());
        Map<Class<?>, ManagedViewTypeImpl<?>> managedViews = new HashMap<>(entityViews.size());

        // For every entity view class, we keep the mapping and the dependent mappings
        // The dependencies are evaluated during initialization and done recursively which is possible because we require no cycles
        Map<Class<?>, ViewMapping> viewMappings = new HashMap<>(entityViews.size());
        Set<Class<?>> dependencies = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>(entityViews.size()));
        for (Class<?> entityViewClass : entityViews) {
            // Check for circular dependencies while initializing subview attribute mappings with view mappings
            dependencies.add(entityViewClass);
            ViewMapping.initializeViewMappings(entityViewClass, entityViewClass, context, viewMappings, dependencies, null);
            dependencies.remove(entityViewClass);
        }
        // Similarly we initialize dependent view types first and cache keep the objects in the mappings
        // Again, this is only possible because we require a cycle free model
        for (ViewMapping viewMapping : viewMappings.values()) {
            ManagedViewTypeImpl<?> managedView = viewMapping.getManagedViewType();

            managedViews.put(viewMapping.getEntityViewClass(), managedView);
            if (managedView instanceof FlatViewType<?>) {
                flatViews.put(viewMapping.getEntityViewClass(), (FlatViewTypeImpl<?>) managedView);
            } else {
                views.put(viewMapping.getEntityViewClass(), (ViewTypeImpl<?>) managedView);
            }
        }

        this.views = Collections.unmodifiableMap(views);
        this.flatViews = Collections.unmodifiableMap(flatViews);
        this.managedViews = Collections.unmodifiableMap(managedViews);

        if (!context.hasErrors()) {
            if (validateExpressions) {
                List<AbstractAttribute<?, ?>> parents = new ArrayList<>();
                for (ManagedViewTypeImpl<?> t : managedViews.values()) {
                    t.checkAttributes(context);
                    t.checkNestedAttributes(parents, context);
                }
            }
        }
    }

    public EntityMetamodel getEntityMetamodel() {
        return metamodel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ViewTypeImpl<X> view(Class<X> clazz) {
        return (ViewTypeImpl<X>) views.get(clazz);
    }

    @Override
    public Set<ViewType<?>> getViews() {
        return new SetView<ViewType<?>>(views.values());
    }

    public Collection<ViewTypeImpl<?>> views() {
        return views.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> ManagedViewTypeImpl<X> managedView(Class<X> clazz) {
        return (ManagedViewTypeImpl<X>) managedViews.get(clazz);
    }

    @Override
    public Set<ManagedViewType<?>> getManagedViews() {
        return new SetView<ManagedViewType<?>>(managedViews.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> FlatViewTypeImpl<X> flatView(Class<X> clazz) {
        return (FlatViewTypeImpl<X>) flatViews.get(clazz);
    }

    @Override
    public Set<FlatViewType<?>> getFlatViews() {
        return new SetView<FlatViewType<?>>(flatViews.values());
    }

}
