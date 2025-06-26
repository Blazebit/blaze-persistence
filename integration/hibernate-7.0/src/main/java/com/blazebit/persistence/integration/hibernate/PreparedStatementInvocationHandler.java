/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.HibernateReturningResult;
import com.blazebit.persistence.integration.hibernate.base.ResultSetInvocationHandler;
import com.blazebit.persistence.spi.DbmsDialect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class PreparedStatementInvocationHandler implements InvocationHandler {

    private final PreparedStatement delegate;
    private final DbmsDialect dbmsDialect;
    private final HibernateReturningResult<?> returningResult;

    public PreparedStatementInvocationHandler(PreparedStatement delegate, DbmsDialect dbmsDialect, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.dbmsDialect = dbmsDialect;
        this.returningResult = returningResult;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("executeQuery".equals(method.getName()) && method.getParameterTypes().length == 0) {
            ResultSet rs;
            HibernateReturningResult<?> result;
            
            if (delegate.execute()) {
                rs = delegate.getResultSet();
                result = returningResult;
            } else {
                result = null;
                returningResult.setUpdateCount(delegate.getUpdateCount());
                rs = dbmsDialect.extractReturningResult(delegate);
            }
            
            return Proxy.newProxyInstance(rs.getClass().getClassLoader(), new Class[]{ ResultSet.class }, new ResultSetInvocationHandler(rs, result));
        }
        
        return method.invoke(delegate, args);
    }

}
