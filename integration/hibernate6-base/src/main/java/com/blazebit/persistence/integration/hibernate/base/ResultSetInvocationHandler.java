/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;


/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class ResultSetInvocationHandler implements InvocationHandler {

    private final ResultSet delegate;
    private final HibernateReturningResult<?> returningResult;
    private final boolean calculateRowCount;
    private int rowCount = 0;

    public ResultSetInvocationHandler(ResultSet delegate, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.returningResult = returningResult;
        this.calculateRowCount = returningResult != null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (calculateRowCount) {
            if ("next".equals(method.getName())) {
                Object result = method.invoke(delegate, args);
                if ((Boolean) result) {
                    rowCount++;
                }
                return result;
            } else if ("close".equals(method.getName())) {
                returningResult.setUpdateCount(rowCount);
            }
        }
        
        return method.invoke(delegate, args);
    }

}
