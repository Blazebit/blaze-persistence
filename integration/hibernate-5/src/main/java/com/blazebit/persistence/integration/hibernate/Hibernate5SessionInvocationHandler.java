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
public class Hibernate5SessionInvocationHandler implements InvocationHandler {
    
    private final SessionImplementor delegate;
    private final Object jdbcCoordinatorProxy;

    public Hibernate5SessionInvocationHandler(SessionImplementor delegate, Object jdbcCoordinatorProxy) {
        this.delegate = delegate;
        this.jdbcCoordinatorProxy = jdbcCoordinatorProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getJdbcCoordinator".equals(method.getName())) {
            return jdbcCoordinatorProxy;
        }
        
        return method.invoke(delegate, args);
    }

}
