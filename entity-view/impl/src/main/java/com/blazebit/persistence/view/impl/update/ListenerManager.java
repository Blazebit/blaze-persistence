/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ListenerManager {

    private final EntityViewManagerImpl evm;
    private final Map<Class<?>, Listeners> listeners;
    private final Map<Class<?>, Listeners> customListeners;

    public ListenerManager(EntityViewManagerImpl evm) {
        this.evm = evm;
        this.listeners = evm.getListeners();
        this.customListeners = new HashMap<>();
    }

    public void onPrePersist(Class<?> entityViewClass, Class<?> entityClass, PrePersistListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPrePersistListener(entityViewClass, listener);
        }
    }

    public void onPrePersist(Class<?> entityViewClass, Class<?> entityClass, PrePersistEntityListener<?, ?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPrePersistEntityListener(entityViewClass, listener);
        }
    }

    public void onPostPersist(Class<?> entityViewClass, Class<?> entityClass, PostPersistListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostPersistListener(entityViewClass, listener);
        }
    }

    public void onPostPersist(Class<?> entityViewClass, Class<?> entityClass, PostPersistEntityListener<?, ?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostPersistEntityListener(entityViewClass, listener);
        }
    }

    public void onPreUpdate(Class<?> entityViewClass, Class<?> entityClass, PreUpdateListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPreUpdateListener(entityViewClass, listener);
        }
    }

    public void onPostUpdate(Class<?> entityViewClass, Class<?> entityClass, PostUpdateListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostUpdateListener(entityViewClass, listener);
        }
    }

    public void onPreRemove(Class<?> entityViewClass, Class<?> entityClass, PreRemoveListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPreRemoveListener(entityViewClass, listener);
        }
    }

    public void onPostRemove(Class<?> entityViewClass, Class<?> entityClass, PostRemoveListener<?> listener) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostRemoveListener(entityViewClass, listener);
        }
    }

    public void onPostCommit(Class<?> entityViewClass, Class<?> entityClass, PostCommitListener<?> listener, Set<ViewTransition> viewTransitions) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostCommitListener(entityViewClass, listener, viewTransitions);
        }
    }

    public void onPostRollback(Class<?> entityViewClass, Class<?> entityClass, PostRollbackListener<?> listener, Set<ViewTransition> viewTransitions) {
        for (Class<?> javaType : evm.getJavaTypeToManagedTypeJavaTypes(entityClass)) {
            Listeners listeners = customListeners.get(javaType);
            if (listeners == null) {
                listeners = new Listeners(javaType);
                customListeners.put(javaType, listeners);
            }
            listeners.addPostRollbackListener(entityViewClass, listener, viewTransitions);
        }
    }

    public boolean hasPostCommitListeners() {
        for (Listeners listener : listeners.values()) {
            if (listener.hasPostCommitListeners()) {
                return true;
            }
        }
        for (Listeners listener : customListeners.values()) {
            if (listener.hasPostCommitListeners()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPostRollbackListeners() {
        for (Listeners listener : listeners.values()) {
            if (listener.hasPostRollbackListeners()) {
                return true;
            }
        }
        for (Listeners listener : customListeners.values()) {
            if (listener.hasPostRollbackListeners()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRemoveListeners(Class<?> entityClass) {
        Listeners listeners = this.listeners.get(entityClass);
        if (listeners != null && listeners.hasRemoveListeners()) {
            return true;
        }
        listeners = customListeners.get(entityClass);
        if (listeners != null && listeners.hasRemoveListeners()) {
            return true;
        }
        return false;
    }

    public boolean hasPossiblyCancellingRemoveListeners(Class<?> entityClass) {
        Listeners listeners = this.listeners.get(entityClass);
        if (listeners != null && listeners.hasPossiblyCancellingRemoveListeners()) {
            return true;
        }
        listeners = customListeners.get(entityClass);
        if (listeners != null && listeners.hasPossiblyCancellingRemoveListeners()) {
            return true;
        }
        return false;
    }

    public void invokePrePersist(UpdateContext context, MutableStateTrackable updatableProxy, Object entity) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(updatableProxy.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        if (listeners != null) {
            listeners.invokePrePersist(context, updatableProxy, entity);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePrePersist(context, updatableProxy, entity);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePrePersist(context, updatableProxy, entity);
        }
    }

    public void invokePostPersist(UpdateContext context, MutableStateTrackable updatableProxy, Object entity) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(updatableProxy.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        if (listeners != null) {
            listeners.invokePostPersist(context, updatableProxy, entity);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostPersist(context, updatableProxy, entity);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostPersist(context, updatableProxy, entity);
        }
    }

    public boolean invokePreUpdate(UpdateContext context, MutableStateTrackable updatableProxy) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(updatableProxy.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        boolean ranAny = false;
        if (listeners != null) {
            ranAny |= listeners.invokePreUpdate(context, updatableProxy);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            ranAny |= listeners.invokePreUpdate(context, updatableProxy);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            ranAny |= listeners.invokePreUpdate(context, updatableProxy);
        }
        return ranAny;
    }

    public void invokePostUpdate(UpdateContext context, MutableStateTrackable updatableProxy) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(updatableProxy.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        if (listeners != null) {
            listeners.invokePostUpdate(context, updatableProxy);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostUpdate(context, updatableProxy);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostUpdate(context, updatableProxy);
        }
    }

    public boolean invokePreRemove(UpdateContext context, EntityViewProxy entityView, Class<?> entityClass, Object entityId) {
        ManagedViewTypeImplementor<?> managedView = null;
        Listeners listeners;
        if (entityView != null) {
            managedView = evm.getMetamodel().managedView(entityView.$$_getEntityViewClass());
            entityClass = managedView.getEntityClass();
            listeners = this.listeners.get(managedView.getJavaType());
            if (listeners != null) {
                if (!listeners.invokePreRemove(context, entityView, entityId)) {
                    return false;
                }
            }
        }
        listeners = this.listeners.get(entityClass);
        if (listeners != null) {
            if (!listeners.invokePreRemove(context, entityView, entityId)) {
                return false;
            }
        }
        listeners = this.customListeners.get(entityClass);
        if (listeners != null) {
            if (!listeners.invokePreRemove(context, entityView, entityId)) {
                return false;
            }
        }
        // Prepare caches for post listeners for remove transition
        if (managedView != null) {
            listeners = this.listeners.get(managedView.getJavaType());
            if (listeners != null) {
                listeners.preparePostRemove(context, entityView, entityId);
            }
        }
        listeners = this.listeners.get(entityClass);
        if (listeners != null) {
            listeners.preparePostRemove(context, entityView, entityId);
        }
        listeners = this.customListeners.get(entityClass);
        if (listeners != null) {
            listeners.preparePostRemove(context, entityView, entityId);
        }
        return true;
    }

    public void invokePostRemove(UpdateContext context, EntityViewProxy entityView, Class<?> entityClass, Object entityId) {
        Listeners listeners;
        if (entityView != null) {
            ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(entityView.$$_getEntityViewClass());
            entityClass = managedView.getEntityClass();
            listeners = this.listeners.get(managedView.getJavaType());
            if (listeners != null) {
                listeners.invokePostRemove(context, entityView, entityId);
            }
        }
        listeners = this.listeners.get(entityClass);
        if (listeners != null) {
            listeners.invokePostRemove(context, entityView, entityId);
        }
        listeners = this.customListeners.get(entityClass);
        if (listeners != null) {
            listeners.invokePostRemove(context, entityView, entityId);
        }
    }

    public void invokePostCommit(UpdateContext context, EntityViewProxy entityView, ViewTransition viewTransition) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(entityView.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        if (listeners != null) {
            listeners.invokePostCommit(context, entityView, viewTransition);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostCommit(context, entityView, viewTransition);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostCommit(context, entityView, viewTransition);
        }
    }

    public void invokePostRollback(UpdateContext context, EntityViewProxy entityView, ViewTransition viewTransition, EntityManager em) {
        ManagedViewTypeImplementor<?> managedView = evm.getMetamodel().managedView(entityView.$$_getEntityViewClass());
        Listeners listeners = this.listeners.get(managedView.getJavaType());
        if (listeners != null) {
            listeners.invokePostRollback(context, entityView, viewTransition, em);
        }
        listeners = this.listeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostRollback(context, entityView, viewTransition, em);
        }
        listeners = this.customListeners.get(managedView.getEntityClass());
        if (listeners != null) {
            listeners.invokePostRollback(context, entityView, viewTransition, em);
        }
    }
}
