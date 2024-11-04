/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.engine.spi.SessionImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate4SessionInvocationHandler implements InvocationHandler {
    
    private final SessionImplementor delegate;
    private final Object transactionCoordinatorProxy;

    public Hibernate4SessionInvocationHandler(SessionImplementor delegate, Object transactionCoordinatorProxy) {
        this.delegate = delegate;
        this.transactionCoordinatorProxy = transactionCoordinatorProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getTransactionCoordinator".equals(method.getName())) {
            return transactionCoordinatorProxy;
        }
        
        return method.invoke(delegate, args);
    }

}
