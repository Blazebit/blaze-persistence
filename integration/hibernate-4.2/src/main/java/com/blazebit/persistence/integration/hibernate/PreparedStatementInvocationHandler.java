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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PreparedStatementInvocationHandler implements InvocationHandler {

    private final PreparedStatement delegate;
    private final DbmsDialect dbmsDialect;
    private final Map<String, Integer> aliasIndex;
    private final HibernateReturningResult<?> returningResult;

    public PreparedStatementInvocationHandler(PreparedStatement delegate, DbmsDialect dbmsDialect, String[][] columns, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.dbmsDialect = dbmsDialect;
        this.aliasIndex = new HashMap<String, Integer>(columns.length);
        this.returningResult = returningResult;
        
        for (int i = 0; i < columns.length; i++) {
            aliasIndex.put(columns[i][1], i + 1);
        }
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
            
            return Proxy.newProxyInstance(rs.getClass().getClassLoader(), new Class[]{ ResultSet.class }, new ResultSetInvocationHandler(rs, aliasIndex, result));
        }
        
        return method.invoke(delegate, args);
    }

}
