/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.tx;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Synchronization;

import com.blazebit.reflection.ExpressionUtils;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate5TransactionSynchronizationStrategy implements TransactionSynchronizationStrategy {
    
    private final EntityTransaction tx;
    private final Object synchronizationRegistry;
    private final Method registerSynchronization;

    public Hibernate5TransactionSynchronizationStrategy(EntityManager em) {
        try {
            this.tx = em.getTransaction();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Could not access entity transaction!", e);
        }
        try {
            Object s = em.unwrap(Class.forName("org.hibernate.Session"));
            this.synchronizationRegistry = ExpressionUtils.getNullSafeValue(s, "transactionCoordinator.localSynchronizations");
            this.registerSynchronization = ReflectionUtils.getMethod(synchronizationRegistry.getClass(), "registerSynchronization", Synchronization.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isActive() {
        return tx.isActive();
    }

    @Override
    public void markRollbackOnly() {
        tx.setRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        try {
            registerSynchronization.invoke(synchronizationRegistry, synchronization);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
