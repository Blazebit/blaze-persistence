/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.StatementPreparer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JdbcCoordinatorInvocationHandler implements InvocationHandler {
    
    private final JdbcCoordinator delegate;
    private final transient StatementPreparer statementPreparer;

    public JdbcCoordinatorInvocationHandler(JdbcCoordinator delegate, StatementPreparer statementPreparer) {
        this.delegate = delegate;
        this.statementPreparer = statementPreparer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getStatementPreparer".equals(method.getName())) {
            return statementPreparer;
        }
        
        return method.invoke(delegate, args);
    }

}
