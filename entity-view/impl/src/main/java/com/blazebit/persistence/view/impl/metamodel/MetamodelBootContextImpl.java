/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.view.PostCommitListener;
import com.blazebit.persistence.view.PostPersistEntityListener;
import com.blazebit.persistence.view.PostPersistListener;
import com.blazebit.persistence.view.PostRemoveListener;
import com.blazebit.persistence.view.PostRollbackListener;
import com.blazebit.persistence.view.PostUpdateListener;
import com.blazebit.persistence.view.PrePersistEntityListener;
import com.blazebit.persistence.view.PrePersistListener;
import com.blazebit.persistence.view.PreRemoveListener;
import com.blazebit.persistence.view.PreUpdateListener;
import com.blazebit.persistence.view.impl.EntityViewListenerClassKey;
import com.blazebit.persistence.view.impl.EntityViewListenerFactory;
import com.blazebit.persistence.view.impl.SimpleEntityViewListenerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MetamodelBootContextImpl implements MetamodelBootContext {

    private static final Class[] LISTENER_CLASSES = {
        PostCommitListener.class,
        PostPersistListener.class,
        PostPersistEntityListener.class,
        PostRemoveListener.class,
        PostRollbackListener.class,
        PostUpdateListener.class,
        PrePersistListener.class,
        PrePersistEntityListener.class,
        PreRemoveListener.class,
        PreUpdateListener.class
    };
    private final Map<Class<?>, ViewMapping> viewMappings;
    private final Map<EntityViewListenerClassKey, EntityViewListenerFactory<?>> viewListeners;
    private final Set<String> errors;

    public MetamodelBootContextImpl() {
        this.viewMappings = new HashMap<>();
        this.viewListeners = new HashMap<>();
        this.errors = new LinkedHashSet<>();
    }

    @Override
    public ViewMapping getViewMapping(Class<?> clazz) {
        return viewMappings.get(clazz);
    }

    @Override
    public void addViewMapping(Class<?> clazz, ViewMapping viewMapping) {
        viewMappings.put(clazz, viewMapping);
    }

    @Override
    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityViewListenerClass) {
        for (EntityViewListenerFactory<?> viewListenerFactory : createViewListenerFactories(entityViewListenerClass)) {
            viewListeners.put(new EntityViewListenerClassKey(entityViewClass, null, viewListenerFactory.getListenerKind(), entityViewListenerClass), viewListenerFactory);
        }
    }

    @Override
    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityClass, Class<?> entityViewListenerClass) {
        for (EntityViewListenerFactory<?> viewListenerFactory : createViewListenerFactories(entityViewListenerClass)) {
            viewListeners.put(new EntityViewListenerClassKey(entityViewClass, entityClass, viewListenerFactory.getListenerKind(), entityViewListenerClass), viewListenerFactory);
        }
    }

    @Override
    public void addEntityViewListener(Class<?> entityViewClass, Class<?> entityClass, EntityViewListenerFactory<?> entityViewListenerFactory) {
        viewListeners.put(new EntityViewListenerClassKey(entityViewClass, entityClass, entityViewListenerFactory.getListenerKind(), entityViewListenerFactory.getListenerClass()), entityViewListenerFactory);
    }

    @Override
    public EntityViewListenerFactory<?>[] createViewListenerFactories(Class<?> entityViewListenerClass) {
        List<EntityViewListenerFactory<?>> factories = new ArrayList<>();
        for (Class<?> listenerClass : LISTENER_CLASSES) {
            if (listenerClass.isAssignableFrom(entityViewListenerClass)) {
                factories.add(new SimpleEntityViewListenerFactory<>(entityViewListenerClass, (Class<? super Object>) listenerClass));
            }
        }

        return factories.toArray(new EntityViewListenerFactory[0]);
    }

    @Override
    public Map<Class<?>, ViewMapping> getViewMappingMap() {
        return viewMappings;
    }

    @Override
    public Collection<ViewMapping> getViewMappings() {
        return viewMappings.values();
    }

    @Override
    public Set<Class<?>> getViewClasses() {
        return viewMappings.keySet();
    }

    @Override
    public Map<EntityViewListenerClassKey, EntityViewListenerFactory<?>> getViewListeners() {
        return viewListeners;
    }

    @Override
    public Set<Class<?>> getViewListenerClasses() {
        Set<Class<?>> viewListenerClasses = new HashSet<>(viewListeners.entrySet().size());
        for (EntityViewListenerClassKey entityViewListenerClassKey : viewListeners.keySet()) {
            viewListenerClasses.add(entityViewListenerClassKey.getEntityViewListenerClass());
        }

        return viewListenerClasses;
    }

    @Override
    public Set<Class<?>> getViewListenerClasses(Class<?> entityViewClass) {
        Set<Class<?>> viewListenerClasses = new HashSet<>(viewListeners.entrySet().size());
        for (EntityViewListenerClassKey entityViewListenerClassKey : viewListeners.keySet()) {
            if (entityViewListenerClassKey.getEntityViewClass() == entityViewClass) {
                viewListenerClasses.add(entityViewListenerClassKey.getEntityViewListenerClass());
            }
        }

        return viewListenerClasses;
    }

    @Override
    public Set<Class<?>> getViewListenerClasses(Class<?> entityViewClass, Class<?> entityClass) {
        Set<Class<?>> viewListenerClasses = new HashSet<>(viewListeners.entrySet().size());
        for (EntityViewListenerClassKey entityViewListenerClassKey : viewListeners.keySet()) {
            if (entityViewListenerClassKey.getEntityViewClass() == entityViewClass && entityViewListenerClassKey.getEntityClass() == entityClass) {
                viewListenerClasses.add(entityViewListenerClassKey.getEntityViewListenerClass());
            }
        }

        return viewListenerClasses;
    }

    @Override
    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public Set<String> getErrors() {
        return errors;
    }
}
