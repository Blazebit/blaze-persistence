/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @since 1.5.0
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
