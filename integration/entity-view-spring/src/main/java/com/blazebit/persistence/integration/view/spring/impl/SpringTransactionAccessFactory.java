/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;

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
