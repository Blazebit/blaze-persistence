/*
 * Copyright 2014 - 2018 Blazebit.
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

import org.hibernate.engine.spi.QueryParameters;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
 * Hibernate always binds positioned parameters before named parameters, which is problematic
 * for any CTE prepended to the query, for which its parameters will be bound out of order.
 * This {@code InvocationHandler} ensures that the indexes used for parameter bindings
 * are adjusted accordingly, effectively swapping the positional query parameters with the
 * named query parameters in the CTE query segment.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
public class SubselectPreparedStatementProxyHandler implements InvocationHandler {

    private final PreparedStatement delegate;
    private final QueryParameters queryParameters;
    private final int cteParameterCount;

    public SubselectPreparedStatementProxyHandler(PreparedStatement delegate, QueryParameters queryParameters, int cteParameterCount) {
        this.delegate = delegate;
        this.queryParameters = queryParameters;
        this.cteParameterCount = cteParameterCount;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith("set") && args.length >= 2) {
            int index = (int) args[0];
            int positionalParameterCount = queryParameters.getFilteredPositionalParameterTypes().length;
            int ctePositionedParameterStartIndex = 1;
            int cteNamedParameterStartIndex = ctePositionedParameterStartIndex + positionalParameterCount;
            int regularNamedParameterStartIndex = cteNamedParameterStartIndex + cteParameterCount;


            boolean isPositionedParamRegular = index < cteNamedParameterStartIndex;
            boolean isNamedParamCte = index < regularNamedParameterStartIndex;
            index += isPositionedParamRegular ? cteParameterCount : isNamedParamCte ? -positionalParameterCount : 0;

            args[0] = index;
        }
        return method.invoke(delegate, args);
    }

}
