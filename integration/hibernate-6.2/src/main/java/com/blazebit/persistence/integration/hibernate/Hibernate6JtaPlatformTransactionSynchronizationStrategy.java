/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionSupport;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

/**
 * @author Moritz Becker
 * @since 1.6.7
 */
public class Hibernate6JtaPlatformTransactionSynchronizationStrategy implements TransactionAccess, TransactionSupport {

    private final JtaPlatform jtaPlatform;
    private final TransactionManager jtaTransactionManager;

    public Hibernate6JtaPlatformTransactionSynchronizationStrategy(JtaPlatform jtaPlatform) {
        this.jtaPlatform = jtaPlatform;
        this.jtaTransactionManager = jtaPlatform.retrieveTransactionManager();
    }

    @Override
    public boolean isActive() {
        try {
            return jtaPlatform.getCurrentStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markRollbackOnly() {
        try {
            jtaTransactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        jtaPlatform.registerSynchronization(synchronization);
    }

    @Override
    public void transactional(Runnable runnable) {
        // In resource local mode, we have no global transaction state
        runnable.run();
    }

}