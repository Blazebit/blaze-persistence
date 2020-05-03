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

package com.blazebit.persistence.integration.hibernate.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;


/**
 * @author Christian Beikov
 * @since 1.5.0
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
