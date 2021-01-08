/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.impl.update.listener.ViewInstancePreRemoveListener;
import com.blazebit.persistence.view.impl.update.listener.ViewPreRemoveListenerImpl;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class Listeners {

    private final Class<?> entityClass;
    private final List<ListenerEntry<PrePersistListener<Object>>> prePersistListeners;
    private final List<ListenerEntry<PrePersistEntityListener<Object, Object>>> prePersistEntityListeners;
    private final List<ListenerEntry<PostPersistListener<Object>>> postPersistListeners;
    private final List<ListenerEntry<PostPersistEntityListener<Object, Object>>> postPersistEntityListeners;
    private final List<ListenerEntry<PreUpdateListener<Object>>> preUpdateListeners;
    private final List<ListenerEntry<PostUpdateListener<Object>>> postUpdateListeners;
    private final List<PreRemoveListenerEntry<Object>> preRemoveListeners;
    private final List<ListenerEntry<PostRemoveListener<Object>>> postRemoveListeners;
    private final List<ListenerTransitionEntry<PostCommitListener<Object>>> postCommitListeners;
    private final List<ListenerTransitionEntry<PostRollbackListener<Object>>> postRollbackListeners;

    public Listeners(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.prePersistListeners = new ArrayList<>();
        this.prePersistEntityListeners = new ArrayList<>();
        this.postPersistListeners = new ArrayList<>();
        this.postPersistEntityListeners = new ArrayList<>();
        this.preUpdateListeners = new ArrayList<>();
        this.postUpdateListeners = new ArrayList<>();
        this.preRemoveListeners = new ArrayList<>();
        this.postRemoveListeners = new ArrayList<>();
        this.postCommitListeners = new ArrayList<>();
        this.postRollbackListeners = new ArrayList<>();
    }

    public void invokePrePersist(UpdateContext context, EntityViewProxy updatableProxy, Object entity) {
        for (int i = 0; i < prePersistListeners.size(); i++) {
            ListenerEntry<PrePersistListener<Object>> entry = prePersistListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.prePersist(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, true, true);
                if (view != null) {
                    entry.listener.prePersist(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view);
                }
            }
        }
        for (int i = 0; i < prePersistEntityListeners.size(); i++) {
            ListenerEntry<PrePersistEntityListener<Object, Object>> entry = prePersistEntityListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.prePersist(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy, entity);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, true, true);
                if (view != null) {
                    entry.listener.prePersist(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view, entity);
                }
            }
        }
    }

    public void invokePostPersist(UpdateContext context, EntityViewProxy updatableProxy, Object entity) {
        for (int i = 0; i < postPersistListeners.size(); i++) {
            ListenerEntry<PostPersistListener<Object>> entry = postPersistListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.postPersist(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, false, false);
                entry.listener.postPersist(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view);
            }
        }
        for (int i = 0; i < postPersistEntityListeners.size(); i++) {
            ListenerEntry<PostPersistEntityListener<Object, Object>> entry = postPersistEntityListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.postPersist(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy, entity);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, false, false);
                entry.listener.postPersist(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view, entity);
            }
        }
    }

    public boolean invokePreUpdate(UpdateContext context, EntityViewProxy updatableProxy) {
        for (int i = 0; i < preUpdateListeners.size(); i++) {
            ListenerEntry<PreUpdateListener<Object>> entry = preUpdateListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.preUpdate(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, false, true);
                entry.listener.preUpdate(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view);
            }
        }
        return !preUpdateListeners.isEmpty();
    }

    public void invokePostUpdate(UpdateContext context, EntityViewProxy updatableProxy) {
        for (int i = 0; i < postUpdateListeners.size(); i++) {
            ListenerEntry<PostUpdateListener<Object>> entry = postUpdateListeners.get(i);
            if (entry.entityViewClass.isInstance(updatableProxy)) {
                entry.listener.postUpdate(context.getEntityViewManager().getSerializableDelegate(updatableProxy.$$_getEntityViewClass()), context.getEntityManager(), updatableProxy);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, updatableProxy, false, false);
                entry.listener.postUpdate(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view);
            }
        }
    }

    public boolean invokePreRemove(UpdateContext context, EntityViewProxy entityView, Object entityId) {
        for (int i = 0; i < preRemoveListeners.size(); i++) {
            PreRemoveListenerEntry<Object> entry = preRemoveListeners.get(i);
            if (entry.entityViewClass.isInstance(entityView)) {
                if (!entry.listener.preRemove(context.getEntityViewManager().getSerializableDelegate(entityView.$$_getEntityViewClass()), context.getEntityManager(), entityView)) {
                    return false;
                }
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, entityView == null ? entityId : entityView, false, true);
                if (!entry.listener.preRemove(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void preparePostRemove(UpdateContext context, EntityViewProxy entityView, Object entityId) {
        for (int i = 0; i < postRemoveListeners.size(); i++) {
            ListenerEntry<PostRemoveListener<Object>> entry = postRemoveListeners.get(i);
            if (!entry.entityViewClass.isInstance(entityView)) {
                context.getEntityView(entry.entityViewClass, entityClass, entityView == null ? entityId : entityView, false, false);
            }
        }
        for (int i = 0; i < postCommitListeners.size(); i++) {
            ListenerTransitionEntry<PostCommitListener<Object>> entry = postCommitListeners.get(i);
            if (entry.viewTransitions.contains(ViewTransition.REMOVE) && !entry.entityViewClass.isInstance(entityView)) {
                context.getEntityView(entry.entityViewClass, entityClass, entityView == null ? entityId : entityView, false, false);
            }
        }
    }

    public void invokePostRemove(UpdateContext context, EntityViewProxy entityView, Object entityId) {
        for (int i = 0; i < postRemoveListeners.size(); i++) {
            ListenerEntry<PostRemoveListener<Object>> entry = postRemoveListeners.get(i);
            if (entry.entityViewClass.isInstance(entityView)) {
                entry.listener.postRemove(context.getEntityViewManager().getSerializableDelegate(entityView.$$_getEntityViewClass()), context.getEntityManager(), entityView);
            } else {
                EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, entityView == null ? entityId : entityView, false, false);
                entry.listener.postRemove(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view);
            }
        }
    }

    public void invokePostCommit(UpdateContext context, EntityViewProxy entityView, ViewTransition viewTransition) {
        for (int i = 0; i < postCommitListeners.size(); i++) {
            ListenerTransitionEntry<PostCommitListener<Object>> entry = postCommitListeners.get(i);
            if (entry.viewTransitions.contains(viewTransition)) {
                if (entry.entityViewClass.isInstance(entityView)) {
                    entry.listener.postCommit(context.getEntityViewManager().getSerializableDelegate(entityView.$$_getEntityViewClass()), context.getEntityManager(), entityView, viewTransition);
                } else {
                    EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, entityView, false, false);
                    entry.listener.postCommit(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view, viewTransition);
                }
            }
        }
    }

    public void invokePostRollback(UpdateContext context, EntityViewProxy entityView, ViewTransition viewTransition, EntityManager em) {
        for (int i = 0; i < postRollbackListeners.size(); i++) {
            ListenerTransitionEntry<PostRollbackListener<Object>> entry = postRollbackListeners.get(i);
            if (entry.viewTransitions.contains(viewTransition)) {
                if (entry.entityViewClass.isInstance(entityView)) {
                    entry.listener.postRollback(context.getEntityViewManager().getSerializableDelegate(entityView.$$_getEntityViewClass()), context.getEntityManager(), entityView, viewTransition);
                } else if (viewTransition != ViewTransition.PERSIST) {
                    // We can't load something that wasn't persisted
                    EntityViewProxy view = context.getEntityView(entry.entityViewClass, entityClass, entityView, false, false, em);
                    entry.listener.postRollback(context.getEntityViewManager().getSerializableDelegate(view.$$_getEntityViewClass()), context.getEntityManager(), view, viewTransition);
                }
            }
        }
    }
        
    public void addPrePersistListener(Class<?> entityViewClass, PrePersistListener<?> listener) {
        prePersistListeners.add(new ListenerEntry<>(entityViewClass, (PrePersistListener<Object>) listener));
    }

    public void addPrePersistEntityListener(Class<?> entityViewClass, PrePersistEntityListener<?, ?> listener) {
        prePersistEntityListeners.add(new ListenerEntry<>(entityViewClass, (PrePersistEntityListener<Object, Object>) listener));
    }

    public void addPostPersistListener(Class<?> entityViewClass, PostPersistListener<?> listener) {
        postPersistListeners.add(new ListenerEntry<>(entityViewClass, (PostPersistListener<Object>) listener));
    }

    public void addPostPersistEntityListener(Class<?> entityViewClass, PostPersistEntityListener<?, ?> listener) {
        postPersistEntityListeners.add(new ListenerEntry<>(entityViewClass, (PostPersistEntityListener<Object, Object>) listener));
    }

    public void addPreUpdateListener(Class<?> entityViewClass, PreUpdateListener<?> listener) {
        preUpdateListeners.add(new ListenerEntry<>(entityViewClass, (PreUpdateListener<Object>) listener));
    }

    public void addPostUpdateListener(Class<?> entityViewClass, PostUpdateListener<?> listener) {
        postUpdateListeners.add(new ListenerEntry<>(entityViewClass, (PostUpdateListener<Object>) listener));
    }

    public void addPreRemoveListener(Class<?> entityViewClass, PreRemoveListener<?> listener) {
        preRemoveListeners.add(new PreRemoveListenerEntry<>(entityViewClass, (PreRemoveListener<Object>) listener));
    }

    public void addPostRemoveListener(Class<?> entityViewClass, PostRemoveListener<?> listener) {
        postRemoveListeners.add(new ListenerEntry<>(entityViewClass, (PostRemoveListener<Object>) listener));
    }

    public void addPostCommitListener(Class<?> entityViewClass, PostCommitListener<?> listener, Set<ViewTransition> viewTransitions) {
        postCommitListeners.add(new ListenerTransitionEntry<>(entityViewClass, viewTransitions, (PostCommitListener<Object>) listener));
    }

    public void addPostRollbackListener(Class<?> entityViewClass, PostRollbackListener<?> listener, Set<ViewTransition> viewTransitions) {
        postRollbackListeners.add(new ListenerTransitionEntry<>(entityViewClass, viewTransitions, (PostRollbackListener<Object>) listener));
    }

    public boolean hasPostCommitListeners() {
        return !postCommitListeners.isEmpty();
    }

    public boolean hasPostRollbackListeners() {
        return !postRollbackListeners.isEmpty();
    }

    public boolean hasRemoveListeners() {
        if (!preRemoveListeners.isEmpty() || !postRemoveListeners.isEmpty()) {
            return true;
        }

        for (int i = 0; i < postCommitListeners.size(); i++) {
            if (postCommitListeners.get(i).viewTransitions.contains(ViewTransition.REMOVE)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPossiblyCancellingRemoveListeners() {
        for (int i = 0; i < preRemoveListeners.size(); i++) {
            if (preRemoveListeners.get(i).possiblyCancelling) {
                return true;
            }
        }

        return false;
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ListenerEntry<T> {
        private final Class<?> entityViewClass;
        private final T listener;

        public ListenerEntry(Class<?> entityViewClass, T listener) {
            this.entityViewClass = entityViewClass;
            this.listener = listener;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class PreRemoveListenerEntry<T> {
        private final Class<?> entityViewClass;
        private final boolean possiblyCancelling;
        private final PreRemoveListener<T> listener;

        public PreRemoveListenerEntry(Class<?> entityViewClass, PreRemoveListener<T> listener) {
            this.entityViewClass = entityViewClass;
            this.listener = listener;
            if (listener instanceof ViewInstancePreRemoveListener) {
                this.possiblyCancelling = ((ViewInstancePreRemoveListener) listener).isPossiblyCancelling();
            } else if (listener instanceof ViewPreRemoveListenerImpl<?>) {
                this.possiblyCancelling = false;
            } else {
                this.possiblyCancelling = true;
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ListenerTransitionEntry<T> {
        private final Class<?> entityViewClass;
        private final Set<ViewTransition> viewTransitions;
        private final T listener;

        public ListenerTransitionEntry(Class<?> entityViewClass, Set<ViewTransition> viewTransitions, T listener) {
            this.entityViewClass = entityViewClass;
            this.viewTransitions = viewTransitions;
            this.listener = listener;
        }
    }
}
