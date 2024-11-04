/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.flush.PostFlushDeleter;
import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;

import javax.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdateContext {

    public EntityViewManagerImpl getEntityViewManager();

    public EntityManager getEntityManager();

    public boolean containsEntity(Class<?> entityClass, Object id);

    public boolean isForceFull();

    public boolean isForceEntity();

    public boolean addVersionCheck(Class<?> entityClass, Object id);

    public boolean addRemovedObject(Object value);

    public void removeRemovedObject(Object value);

    public boolean isRemovedObject(Object value);

    public TransactionAccess getTransactionAccess();

    public InitialStateResetter getInitialStateResetter();

    public List<PostFlushDeleter> getOrphanRemovalDeleters();

    public void removeOrphans(int orphanRemovalStartIndex);

    public void invokePrePersist(MutableStateTrackable updatableProxy, Object entity);

    public void invokePostPersist(MutableStateTrackable updatableProxy, Object entity);

    public boolean invokePreUpdate(MutableStateTrackable updatableProxy);

    public void invokePostUpdate(MutableStateTrackable updatableProxy);

    public boolean invokePreRemove(EntityViewProxy entityViewProxy);

    public boolean invokePreRemove(Class<?> entityClass, Object entityId);

    public void invokePostRemove(EntityViewProxy entityView);

    public void invokePostRemove(Class<?> entityClass, Object entityId);

    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object updatableProxy, boolean convertOnly, boolean prePhase);

    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object updatableProxy, boolean convertOnly, boolean prePhase, EntityManager entityManager);

    public boolean hasRemoveListeners(Class<?> elementEntityClass);

    public boolean hasPossiblyCancellingRemoveListeners(Class<?> elementEntityClass);
}
