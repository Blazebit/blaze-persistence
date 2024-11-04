/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Hibernate5SessionFactoryInvocationHandler implements InvocationHandler {

    private final SessionFactoryImplementor delegate;
    private final Object dialect;

    public Hibernate5SessionFactoryInvocationHandler(SessionFactoryImplementor delegate, Object dialect) {
        this.delegate = delegate;
        this.dialect = dialect;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getDialect".equals(method.getName())) {
            return dialect;
        }
        
        return method.invoke(delegate, args);
    }

}
