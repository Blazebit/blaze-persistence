/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.spi.TransactionSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SpringTransactionSupport implements TransactionSupport {

    private final PlatformTransactionManager tm;

    public SpringTransactionSupport(PlatformTransactionManager tm) {
        this.tm = tm;
    }

    @Override
    public void transactional(final Runnable runnable) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                runnable.run();
                return null;
            }
        });
    }

}
