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

package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.spi.TransactionAccess;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Status;
import javax.transaction.Synchronization;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SpringTransactionSynchronizationStrategy implements TransactionAccess {

    public static final SpringTransactionSynchronizationStrategy INSTANCE = new SpringTransactionSynchronizationStrategy();

    private SpringTransactionSynchronizationStrategy() {
    }

    @Override
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public void markRollbackOnly() {
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationWrapper(synchronization));
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class TransactionSynchronizationWrapper implements TransactionSynchronization {

        private final Synchronization synchronization;

        public TransactionSynchronizationWrapper(Synchronization synchronization) {
            this.synchronization = synchronization;
        }

        @Override
        public void suspend() {
            // No-op
        }

        @Override
        public void resume() {
            // No-op
        }

        @Override
        public void flush() {
            // No-op
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            // No-op
        }

        @Override
        public void beforeCompletion() {
            synchronization.beforeCompletion();
        }

        @Override
        public void afterCommit() {
            // No-op
        }

        @Override
        public void afterCompletion(int status) {
            switch (status) {
                case TransactionSynchronization.STATUS_COMMITTED:
                    status = Status.STATUS_COMMITTED;
                    break;
                case TransactionSynchronization.STATUS_ROLLED_BACK:
                    status = Status.STATUS_ROLLEDBACK;
                    break;
                default:
                    status = Status.STATUS_UNKNOWN;
                    break;
            }
            synchronization.afterCompletion(status);
        }
    }

}
