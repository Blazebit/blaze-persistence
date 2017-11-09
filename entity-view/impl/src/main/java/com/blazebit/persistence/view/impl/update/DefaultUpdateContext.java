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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.tx.TransactionHelper;
import com.blazebit.persistence.view.impl.tx.TransactionSynchronizationStrategy;

import javax.persistence.EntityManager;
import javax.transaction.Synchronization;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultUpdateContext implements UpdateContext {

    private final EntityViewManagerImpl evm;
    private final EntityManager em;
    private final boolean forceFull;
    private final TransactionSynchronizationStrategy synchronizationStrategy;
    private final InitialStateResetter initialStateResetter;
    private Map<Object, Object> removedObjects;
    private Set<EntityKey> versionChecked;

    public DefaultUpdateContext(EntityViewManagerImpl evm, EntityManager em, boolean forceFull) {
        this.evm = evm;
        this.em = em;
        this.forceFull = forceFull;
        this.synchronizationStrategy = TransactionHelper.getSynchronizationStrategy(em);

        if (!synchronizationStrategy.isActive()) {
            throw new IllegalStateException("Transaction is not active!");
        }

        this.initialStateResetter = new ResetInitialStateSynchronization();
        synchronizationStrategy.registerSynchronization((Synchronization) initialStateResetter);
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
        return forceFull;
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
    public boolean isRemovedObject(Object value) {
        return removedObjects != null && removedObjects.containsKey(value);
    }

    public TransactionSynchronizationStrategy getSynchronizationStrategy() {
        return synchronizationStrategy;
    }

    @Override
    public InitialStateResetter getInitialStateResetter() {
        return initialStateResetter;
    }

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

            if (!entityClass.equals(entityKey.entityClass)) {
                return false;
            }
            return entityId.equals(entityKey.entityId);
        }

        @Override
        public int hashCode() {
            int result = entityClass.hashCode();
            result = 31 * result + entityId.hashCode();
            return result;
        }
    }

}
