/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.tx.EntityViewInvocationContextWrapper;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.tx.TransactionalQueryRunner} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class TransactionalEntityViewQueryRunner implements EntityViewQueryRunner {

    @Inject
    private TransactionStrategy strategy;

    @Inject
    private ActiveEntityManagerHolder activeEntityManagerHolder;

    @Override
    public Object executeQuery(final EntityViewQueryBuilder builder, final EntityViewCdiQueryInvocationContext context)
            throws Throwable {
        if (context.getRepositoryMethodMetadata().isRequiresTransaction()) {
            try {
                activeEntityManagerHolder.set(context.getEntityManager());
                return executeTransactional(builder, context);
            } finally {
                activeEntityManagerHolder.dispose();
            }
        }
        return executeNonTransactional(builder, context);
    }

    protected Object executeNonTransactional(final EntityViewQueryBuilder builder, final EntityViewCdiQueryInvocationContext context) {
        return builder.executeQuery(context);
    }

    protected Object executeTransactional(final EntityViewQueryBuilder builder, final EntityViewCdiQueryInvocationContext context)
            throws Exception {
        return strategy.execute(new EntityViewInvocationContextWrapper(context) {
            @Override
            public Object proceed() throws Exception {
                return builder.executeQuery(context);
            }
        });
    }

}