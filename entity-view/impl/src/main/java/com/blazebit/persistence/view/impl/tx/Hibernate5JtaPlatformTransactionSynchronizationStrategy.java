/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;
import com.blazebit.reflection.ReflectionUtils;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class Hibernate5JtaPlatformTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {

    private final Object jtaPlatform;
    private final Object jtaTransactionManager;
    private final Method getCurrentStatusMethod;
    private final Method registerSynchronizationMethod;
    private final Method setRollbackOnlyMethod;

    public Hibernate5JtaPlatformTransactionSynchronizationStrategy(Object jtaPlatform) {
        this.jtaPlatform = jtaPlatform;
        try {
            this.jtaTransactionManager = ReflectionUtils.getMethod(jtaPlatform.getClass(), "retrieveTransactionManager").invoke(jtaPlatform);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        this.getCurrentStatusMethod = ReflectionUtils.getMethod(jtaPlatform.getClass(), "getCurrentStatus");
        this.registerSynchronizationMethod = ReflectionUtils.getMethod(jtaPlatform.getClass(), "registerSynchronization", Synchronization.class);
        this.setRollbackOnlyMethod = ReflectionUtils.getMethod(jtaTransactionManager.getClass(), "setRollbackOnly");
    }

    @Override
    public boolean isActive() {
        try {
            return (int) getCurrentStatusMethod.invoke(jtaPlatform) == Status.STATUS_ACTIVE;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markRollbackOnly() {
        try {
            setRollbackOnlyMethod.invoke(jtaTransactionManager);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        try {
            registerSynchronizationMethod.invoke(jtaPlatform, synchronization);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transactional(Runnable runnable) {
        // In resource local mode, we have no global transaction state
        runnable.run();
    }

}