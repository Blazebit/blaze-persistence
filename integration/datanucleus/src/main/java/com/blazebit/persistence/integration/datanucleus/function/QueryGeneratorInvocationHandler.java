/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus.function;

import org.datanucleus.NucleusContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author Christian
 * @since 1.2.0
 */
public class QueryGeneratorInvocationHandler implements InvocationHandler {

    private final NucleusContext context;

    public QueryGeneratorInvocationHandler(NucleusContext context) {
        this.context = context;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getClassLoaderResolver".equals(method.getName())) {
            return context.getClassLoaderResolver(null);
        }
        return null;
    }
}
