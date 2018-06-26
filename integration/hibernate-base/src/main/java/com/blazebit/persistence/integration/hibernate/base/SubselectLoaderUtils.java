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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
public class SubselectLoaderUtils {

    private static final Constructor<? extends PreparedStatement> PROXY_CONSTRUCTOR;

    static {
        try {
            Class<? extends PreparedStatement> proxyClass = (Class<? extends PreparedStatement>)
                    Proxy.getProxyClass(SubselectPreparedStatementProxyHandler.class.getClassLoader(), PreparedStatement.class);
            PROXY_CONSTRUCTOR = proxyClass.getConstructor(InvocationHandler.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private SubselectLoaderUtils() {
    }

    public static PreparedStatement getPreparedStatementProxy(PreparedStatement delegate, QueryParameters queryParameters, int cteParameterCount) {
        try {
            return PROXY_CONSTRUCTOR.newInstance(new SubselectPreparedStatementProxyHandler(delegate, queryParameters, cteParameterCount));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static int applyCteAndCountParameters(String originalSql, StringBuilder sb) {
        int brackets = 0;
        int cteParameterCount = 0;
        boolean cteMode = false;

        for (int i = 0; i < originalSql.length(); i++) {
            final char c = originalSql.charAt(i);
            if (c == '(') {
                brackets++;
            } else if (c == ')') {
                brackets--;
                if (brackets == 0) {
                    cteMode = !cteMode;
                }
            } else if (c == '?') {
                cteParameterCount++;
            }

            if (!cteMode && brackets == 0 && originalSql.regionMatches(true, i, "select ", 0, "select ".length())) {
                break;
            }

            sb.append(c);
        }

        return cteParameterCount;
    }

}
