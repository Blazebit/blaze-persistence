/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;
import com.blazebit.persistence.deltaspike.data.impl.tx.EntityViewInvocationContextWrapper;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.data.impl.handler.EntityRepositoryHandler;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Method;

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
        if (needsTransaction(context)) {
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

    private boolean needsTransaction(EntityViewCdiQueryInvocationContext context) {
        boolean requiresTx = false;
        Method method = context.getMethod();
        if (ClassUtils.containsPossiblyGenericMethod(EntityRepositoryHandler.class, method)) {
            Method executed = ClassUtils.extractPossiblyGenericMethod(EntityRepositoryHandler.class, method);
            requiresTx = executed.isAnnotationPresent(RequiresTransaction.class);
        }
        return requiresTx || context.getRepositoryMethod().requiresTransaction();
    }
}