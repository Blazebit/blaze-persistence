/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.view.spi.TransactionAccessFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SpringTransactionAccessFactory implements TransactionAccessFactory {

    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return SpringTransactionSynchronizationStrategy.INSTANCE;
        }
        return null;
    }

    @Override
    public int getPriority() {
        return 99;
    }
}
