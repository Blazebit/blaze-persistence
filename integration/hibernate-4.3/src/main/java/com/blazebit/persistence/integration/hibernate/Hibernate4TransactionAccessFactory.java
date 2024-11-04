/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.view.spi.TransactionAccess;
import com.blazebit.persistence.view.spi.TransactionAccessFactory;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@ServiceProvider(TransactionAccessFactory.class)
public class Hibernate4TransactionAccessFactory implements TransactionAccessFactory {

    @Override
    public TransactionAccess createTransactionAccess(EntityManager entityManager) {
        return new Hibernate4TransactionSynchronizationStrategy(entityManager);
    }

    @Override
    public int getPriority() {
        return 100;
    }
}