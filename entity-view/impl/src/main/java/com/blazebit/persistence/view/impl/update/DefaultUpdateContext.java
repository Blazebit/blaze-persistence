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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.FlushOperationBuilder;
import com.blazebit.persistence.view.OptimisticLockException;
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
import com.blazebit.persistence.view.ViewAndEntityListener;
import com.blazebit.persistence.view.ViewListener;
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.ViewTransitionListener;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.tx.TransactionHelper;
import com.blazebit.persistence.view.impl.update.flush.PostFlushDeleter;
import com.blazebit.persistence.view.impl.update.listener.ViewAndEntityPostPersistListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewAndEntityPrePersistListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPostCommitListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPostPersistListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPostRemoveListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPostRollbackListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPostUpdateListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPrePersistListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPreRemoveListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewPreUpdateListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewTransitionPostCommitListenerImpl;
import com.blazebit.persistence.view.impl.update.listener.ViewTransitionPostRollbackListenerImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.EntityManager;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultUpdateContext implements UpdateContext, FlushOperationBuilder {

    private static final Set<ViewTransition> VIEW_TRANSITIONS = EnumSet.allOf(ViewTransition.class);

    private final EntityViewManagerImpl evm;
    private final EntityManager em;
    private final boolean forceFull;
    private final boolean forceEntity;
    private final boolean remove;
    private final Class<?> entityViewClass;
    private final Object object;
    private final Object entity;
    private final TransactionAccess transactionAccess;
    private final InitialStateResetter initialStateResetter;
    private final ListenerManager listenerManager;
    private Map<Object, Object> removedObjects;
    private Map<EntityKey, List<ViewCacheEntry>> viewCache;
    private Set<EntityKey> versionChecked;
    private List<PostFlushDeleter> orphanRemovalDeleters = new ArrayList<>();

    public DefaultUpdateContext(EntityViewManagerImpl evm, EntityManager em, boolean forceFull, boolean forceEntity, boolean remove, Class<?> entityViewClass, Object object, Object entity) {
        this.evm = evm;
        this.em = em;
        this.forceFull = forceFull;
        this.transactionAccess = TransactionHelper.getTransactionAccess(em);
        this.forceEntity = forceEntity;
        this.remove = remove;
        this.entityViewClass = entityViewClass;
        this.object = object;
        this.entity = entity;

        if (!transactionAccess.isActive()) {
            throw new IllegalStateException("Transaction is not active!");
        }

        this.listenerManager = new ListenerManager(evm);
        this.initialStateResetter = new ResetInitialStateSynchronization(this, listenerManager);
        transactionAccess.registerSynchronization((Synchronization) initialStateResetter);
    }

    @Override
    public EntityViewManagerImpl getEntityViewManager() {
        return evm;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public boolean containsEntity(Class<?> entityClass, Object id) {
        return evm.getJpaProvider().containsEntity(em, entityClass, id);
    }

    @Override
    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object o, boolean convertOnly, boolean prePhase) {
        return getEntityView(viewType, entityClass, o, convertOnly, prePhase, em);
    }

    @Override
    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object o, boolean convertOnly, boolean prePhase, EntityManager em) {
        EntityViewProxy view;
        Object entityId;
        if (o instanceof EntityViewProxy) {
            view = (EntityViewProxy) o;
            entityId = evm.getEntityId(this.em, view);
            entityClass = view.$$_getJpaManagedClass();
        } else {
            view = null;
            entityId = o;
        }
        if (viewCache == null) {
            viewCache = new HashMap<>();
        }
        EntityKey entityKey = new EntityKey(entityClass, entityId);
        List<ViewCacheEntry> cachedViews = viewCache.get(entityKey);
        if (cachedViews == null) {
            cachedViews = new ArrayList<>();
            viewCache.put(entityKey, cachedViews);
        } else {
            Map<Class<?>, Set<Class<?>>> convertibleManagedViewTypes = evm.getConvertibleManagedViewTypes();
            EntityViewProxy conversionCandidate = null;
            for (ViewCacheEntry entry : cachedViews) {
                if (prePhase == entry.fromPrePhase) {
                    if (viewType.isInstance(entry.view)) {
                        return entry.view;
                    } else if (conversionCandidate == null && convertibleManagedViewTypes.get(entry.view.$$_getEntityViewClass()).contains(viewType)) {
                        conversionCandidate = entry.view;
                    }
                }
            }
            if (conversionCandidate != null) {
                conversionCandidate = (EntityViewProxy) evm.convert(conversionCandidate, viewType);
                cachedViews.add(new ViewCacheEntry(conversionCandidate, prePhase));
                return conversionCandidate;
            }
        }
        if (view != null && evm.getConvertibleManagedViewTypes().get(view.$$_getEntityViewClass()).contains(viewType)) {
            view = (EntityViewProxy) evm.convert(view, viewType);
            cachedViews.add(new ViewCacheEntry(view, prePhase));
            return view;
        }
        if (convertOnly) {
            return null;
        }
        view = (EntityViewProxy) evm.find(em, viewType, entityId);
        if (view == null) {
            throw new OptimisticLockException("Could not fetch view of type [" + viewType.getName() + "] with entity id [" + entityId + "], which is required for an entity view lifecycle listener, because it appears to have been deleted already!", o instanceof EntityViewProxy ? null : o, o instanceof EntityViewProxy ? o : null);
        }
        cachedViews.add(new ViewCacheEntry(view, prePhase));
        return view;
    }

    @Override
    public boolean hasRemoveListeners(Class<?> entityClass) {
        return listenerManager.hasRemoveListeners(entityClass);
    }

    @Override
    public boolean hasPossiblyCancellingRemoveListeners(Class<?> elementEntityClass) {
        return listenerManager.hasPossiblyCancellingRemoveListeners(elementEntityClass);
    }

    @Override
    public boolean isForceFull() {
        return forceFull;
    }

    @Override
    public boolean isForceEntity() {
        return forceEntity;
    }

    @Override
    public boolean addVersionCheck(Class<?> entityClass, Object id) {
        if (versionChecked == null) {
            versionChecked = new HashSet<>();
        }
        return versionChecked.add(new EntityKey(entityClass, id));
    }

    @Override
    public boolean addRemovedObject(Object value) {
        if (removedObjects == null) {
            removedObjects = new IdentityHashMap<>();
        }
        return removedObjects.put(value, value) == null;
    }

    @Override
    public void removeRemovedObject(Object value) {
        removedObjects.remove(value);
    }

    @Override
    public boolean isRemovedObject(Object value) {
        return removedObjects != null && removedObjects.containsKey(value);
    }

    public TransactionAccess getTransactionAccess() {
        return transactionAccess;
    }

    @Override
    public InitialStateResetter getInitialStateResetter() {
        return initialStateResetter;
    }

    @Override
    public List<PostFlushDeleter> getOrphanRemovalDeleters() {
        return orphanRemovalDeleters;
    }

    @Override
    public void removeOrphans(int orphanRemovalStartIndex) {
        for (int i = orphanRemovalStartIndex; i < orphanRemovalDeleters.size(); i++) {
            PostFlushDeleter postFlushDeleter = orphanRemovalDeleters.get(i);
            postFlushDeleter.execute(this);
        }
    }

    @Override
    public void invokePrePersist(MutableStateTrackable updatableProxy, Object entity) {
        listenerManager.invokePrePersist(this, updatableProxy, entity);
    }

    @Override
    public void invokePostPersist(MutableStateTrackable updatableProxy, Object entity) {
        listenerManager.invokePostPersist(this, updatableProxy, entity);
    }

    @Override
    public boolean invokePreUpdate(MutableStateTrackable updatableProxy) {
        return listenerManager.invokePreUpdate(this, updatableProxy);
    }

    @Override
    public void invokePostUpdate(MutableStateTrackable updatableProxy) {
        listenerManager.invokePostUpdate(this, updatableProxy);
    }

    @Override
    public boolean invokePreRemove(EntityViewProxy entityViewProxy) {
        return listenerManager.invokePreRemove(this, entityViewProxy, null, null);
    }

    @Override
    public boolean invokePreRemove(Class<?> entityClass, Object entityId) {
        return listenerManager.invokePreRemove(this, null, entityClass, entityId);
    }

    @Override
    public void invokePostRemove(EntityViewProxy entityView) {
        listenerManager.invokePostRemove(this, entityView, null, null);
    }

    @Override
    public void invokePostRemove(Class<?> entityClass, Object entityId) {
        listenerManager.invokePostRemove(this, null, entityClass, entityId);
    }

    @Override
    public FlushOperationBuilder onPrePersist(PrePersistListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PrePersistListener.class);
        listenerManager.onPrePersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPrePersist(PrePersistEntityListener<?, ?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PrePersistEntityListener.class);
        Class<?> listenerEntityClass = evm.getListenerEntityClass(listener.getClass(), PrePersistEntityListener.class);
        listenerManager.onPrePersist(managedView, listenerEntityClass, listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostPersist(PostPersistListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostPersistListener.class);
        listenerManager.onPostPersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostPersist(PostPersistEntityListener<?, ?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostPersistEntityListener.class);
        Class<?> listenerEntityClass = evm.getListenerEntityClass(listener.getClass(), PostPersistEntityListener.class);
        listenerManager.onPostPersist(managedView, listenerEntityClass, listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPreUpdate(PreUpdateListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PreUpdateListener.class);
        listenerManager.onPreUpdate(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostUpdate(PostUpdateListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostUpdateListener.class);
        listenerManager.onPostUpdate(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPreRemove(PreRemoveListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PreRemoveListener.class);
        listenerManager.onPreRemove(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostRemove(PostRemoveListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostRemoveListener.class);
        listenerManager.onPostRemove(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostCommit(PostCommitListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostCommitListener.class);
        listenerManager.onPostCommit(managedView, managedView.getEntityClass(), listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostCommit(Set<ViewTransition> viewTransitions, PostCommitListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostCommitListener.class);
        listenerManager.onPostCommit(managedView, managedView.getEntityClass(), listener, viewTransitions);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostRollback(PostRollbackListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostRollbackListener.class);
        listenerManager.onPostRollback(managedView, managedView.getEntityClass(), listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public FlushOperationBuilder onPostRollback(Set<ViewTransition> viewTransitions, PostRollbackListener<?> listener) {
        ManagedViewType<?> managedView = evm.getListenerManagedView(listener.getClass(), PostRollbackListener.class);
        listenerManager.onPostRollback(managedView, managedView.getEntityClass(), listener, viewTransitions);
        return this;
    }

    private <T> ManagedViewTypeImplementor<T> managedView(Class<T> entityViewClass) {
        ManagedViewTypeImplementor<T> managedView = evm.getMetamodel().managedView(entityViewClass);
        if (managedView == null) {
            // It could be the implementation class
            managedView = evm.getMetamodel().managedView(evm.getProxyFactory().getEntityViewClass(entityViewClass));
            if (managedView == null) {
                throw new IllegalArgumentException("Can't add a listener for the non-view type: " + entityViewClass.getName());
            }
        }

        return managedView;
    }

    @Override
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, PrePersistListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPrePersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, PrePersistEntityListener<T, ?> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPrePersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, PostPersistListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostPersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, PostPersistEntityListener<T, ?> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostPersist(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, PreUpdateListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPreUpdate(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, PostUpdateListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostUpdate(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, PreRemoveListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPreRemove(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, PostRemoveListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRemove(managedView, managedView.getEntityClass(), listener);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, PostCommitListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostCommit(managedView, managedView.getEntityClass(), listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, PostCommitListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostCommit(managedView, managedView.getEntityClass(), listener, viewTransitions);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, PostRollbackListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRollback(managedView, managedView.getEntityClass(), listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, PostRollbackListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRollback(managedView, managedView.getEntityClass(), listener, viewTransitions);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, PrePersistListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPrePersist(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, PrePersistEntityListener<T, E> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPrePersist(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, PostPersistListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostPersist(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, PostPersistEntityListener<T, E> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostPersist(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, Class<E> entityClass, PreUpdateListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPreUpdate(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, Class<E> entityClass, PostUpdateListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostUpdate(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, Class<E> entityClass, PreRemoveListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPreRemove(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, Class<E> entityClass, PostRemoveListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRemove(managedView, entityClass, listener);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostCommit(managedView, entityClass, listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, PostCommitListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostCommit(managedView, entityClass, listener, viewTransitions);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRollback(managedView, entityClass, listener, VIEW_TRANSITIONS);
        return this;
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, PostRollbackListener<T> listener) {
        ManagedViewTypeImplementor<T> managedView = managedView(entityViewClass);
        listenerManager.onPostRollback(managedView, entityClass, listener, viewTransitions);
        return this;
    }

    @Override
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPrePersist(entityViewClass, new ViewPrePersistListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, ViewAndEntityListener<T, ?> listener) {
        return onPrePersist(entityViewClass, new ViewAndEntityPrePersistListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostPersist(entityViewClass, new ViewPostPersistListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, ViewAndEntityListener<T, ?> listener) {
        return onPostPersist(entityViewClass, new ViewAndEntityPostPersistListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPreUpdate(entityViewClass, new ViewPreUpdateListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostUpdate(entityViewClass, new ViewPostUpdateListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPreRemove(entityViewClass, new ViewPreRemoveListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostRemove(entityViewClass, new ViewPostRemoveListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, new ViewPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, viewTransitions, new ViewPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, new ViewPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, viewTransitions, new ViewPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPrePersist(entityViewClass, entityClass, new ViewPrePersistListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPrePersist(Class<T> entityViewClass, Class<E> entityClass, ViewAndEntityListener<T, E> listener) {
        return onPrePersist(entityViewClass, entityClass, new ViewAndEntityPrePersistListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostPersist(entityViewClass, entityClass, new ViewPostPersistListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostPersist(Class<T> entityViewClass, Class<E> entityClass, ViewAndEntityListener<T, E> listener) {
        return onPostPersist(entityViewClass, entityClass, new ViewAndEntityPostPersistListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPreUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPreUpdate(entityViewClass, entityClass, new ViewPreUpdateListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostUpdate(entityViewClass, entityClass, new ViewPostUpdateListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPreRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPreRemove(entityViewClass, entityClass, new ViewPreRemoveListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostRemove(entityViewClass, entityClass, new ViewPostRemoveListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, new ViewPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, viewTransitions, new ViewPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, new ViewPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, viewTransitions, new ViewPostRollbackListenerImpl<>(listener));
    }

    @Override
    public void flush() {
        if (remove) {
            if (entityViewClass == null) {
                evm.remove(this, object);
            } else {
                evm.remove(this, entityViewClass, object);
            }
        } else {
            if (entity == null) {
                evm.update(this, object);
            } else {
                evm.updateTo(this, object, entity);
            }
        }
    }

    @Override
    public FlushOperationBuilder onPostCommitPersist(PostCommitListener<?> listener) {
        return onPostCommit(EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public FlushOperationBuilder onPostCommitUpdate(PostCommitListener<?> listener) {
        return onPostCommit(EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public FlushOperationBuilder onPostCommitRemove(PostCommitListener<?> listener) {
        return onPostCommit(EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public FlushOperationBuilder onPostRollbackPersist(PostRollbackListener<?> listener) {
        return onPostRollback(EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public FlushOperationBuilder onPostRollbackUpdate(PostRollbackListener<?> listener) {
        return onPostRollback(EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public FlushOperationBuilder onPostRollbackRemove(PostRollbackListener<?> listener) {
        return onPostRollback(EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, Class<E> entityClass, PostCommitListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, Class<E> entityClass, PostRollbackListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommitRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackPersist(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.PERSIST), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackUpdate(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.UPDATE), listener);
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollbackRemove(Class<T> entityViewClass, Class<E> entityClass, ViewListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, EnumSet.of(ViewTransition.REMOVE), listener);
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, ViewTransitionListener<T> listener) {
        return onPostCommit(entityViewClass, VIEW_TRANSITIONS, new ViewTransitionPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener) {
        return onPostCommit(entityViewClass, viewTransitions, new ViewTransitionPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, ViewTransitionListener<T> listener) {
        return onPostRollback(entityViewClass, VIEW_TRANSITIONS, new ViewTransitionPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener) {
        return onPostRollback(entityViewClass, viewTransitions, new ViewTransitionPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, ViewTransitionListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, VIEW_TRANSITIONS, new ViewTransitionPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostCommit(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener) {
        return onPostCommit(entityViewClass, entityClass, viewTransitions, new ViewTransitionPostCommitListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, ViewTransitionListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, VIEW_TRANSITIONS, new ViewTransitionPostRollbackListenerImpl<>(listener));
    }

    @Override
    public <T, E> FlushOperationBuilder onPostRollback(Class<T> entityViewClass, Class<E> entityClass, Set<ViewTransition> viewTransitions, ViewTransitionListener<T> listener) {
        return onPostRollback(entityViewClass, entityClass, viewTransitions, new ViewTransitionPostRollbackListenerImpl<>(listener));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class EntityKey {
        private final Class<?> entityClass;
        private final Object entityId;

        public EntityKey(Class<?> entityClass, Object entityId) {
            this.entityClass = entityClass;
            this.entityId = entityId;
        }

        @Override
        public boolean equals(Object o) {
            EntityKey entityKey = (EntityKey) o;
            return entityClass.equals(entityKey.entityClass) && entityId.equals(entityKey.entityId);
        }

        @Override
        public int hashCode() {
            int result = entityClass.hashCode();
            result = 31 * result + entityId.hashCode();
            return result;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ViewCacheEntry {
        private final EntityViewProxy view;
        private final boolean fromPrePhase;

        public ViewCacheEntry(EntityViewProxy view, boolean fromPrePhase) {
            this.view = view;
            this.fromPrePhase = fromPrePhase;
        }
    }

}
