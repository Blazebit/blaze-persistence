/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.sql.exec.spi.ExecutionContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class Hibernate7ExecutionContextInvocationHandler implements InvocationHandler {

    private final ExecutionContext delegate;
    private final Object session;

    public Hibernate7ExecutionContextInvocationHandler(ExecutionContext delegate, Object session) {
        this.delegate = delegate;
        this.session = session;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getSession".equals(method.getName())) {
            return session;
        }
        
        return method.invoke(delegate, args);
    }

}
