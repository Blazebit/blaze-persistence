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
 * @since 1.3.0
 */
public class SimpleUpdateContext implements UpdateContext {

    private final EntityViewManagerImpl evm;
    private final EntityManager em;

    public SimpleUpdateContext(EntityViewManagerImpl evm, EntityManager em) {
        this.evm = evm;
        this.em = em;
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
    public boolean isForceFull() {
        return false;
    }

    @Override
    public boolean isForceEntity() {
        return false;
    }

    @Override
    public boolean addVersionCheck(Class<?> entityClass, Object id) {
        return false;
    }

    @Override
    public boolean addRemovedObject(Object value) {
        return false;
    }

    @Override
    public void removeRemovedObject(Object value) {
    }

    @Override
    public boolean isRemovedObject(Object value) {
        return false;
    }

    public TransactionAccess getTransactionAccess() {
        return null;
    }

    @Override
    public InitialStateResetter getInitialStateResetter() {
        return null;
    }

    @Override
    public List<PostFlushDeleter> getOrphanRemovalDeleters() {
        return null;
    }

    @Override
    public void removeOrphans(int orphanRemovalStartIndex) {
    }

    @Override
    public void invokePrePersist(MutableStateTrackable updatableProxy, Object entity) {
    }

    @Override
    public void invokePostPersist(MutableStateTrackable updatableProxy, Object entity) {
    }

    @Override
    public boolean invokePreUpdate(MutableStateTrackable updatableProxy) {
        return false;
    }

    @Override
    public void invokePostUpdate(MutableStateTrackable updatableProxy) {
    }

    @Override
    public boolean invokePreRemove(EntityViewProxy entityViewProxy) {
        return true;
    }

    @Override
    public boolean invokePreRemove(Class<?> entityClass, Object entityId) {
        return true;
    }

    @Override
    public void invokePostRemove(EntityViewProxy entityView) {
    }

    @Override
    public void invokePostRemove(Class<?> entityClass, Object entityId) {
    }

    @Override
    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object updatableProxy, boolean convertOnly, boolean prePhase) {
        return null;
    }

    @Override
    public EntityViewProxy getEntityView(Class<?> viewType, Class<?> entityClass, Object updatableProxy, boolean convertOnly, boolean prePhase, EntityManager entityManager) {
        return null;
    }

    @Override
    public boolean hasRemoveListeners(Class<?> elementEntityClass) {
        return false;
    }

    @Override
    public boolean hasPossiblyCancellingRemoveListeners(Class<?> elementEntityClass) {
        return false;
    }
}
