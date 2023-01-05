/*
 * Copyright 2014 - 2023 Blazebit.
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

    public static PreparedStatement getPreparedStatementProxy(PreparedStatement delegate, QueryParameters queryParameters, int cteParameterCount, int selectParameterCount) {
        try {
            return PROXY_CONSTRUCTOR.newInstance(new SubselectPreparedStatementProxyHandler(delegate, queryParameters, cteParameterCount, selectParameterCount));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static int applyCteAndCountParameters(String originalSql, StringBuilder sb) {
        int brackets = 0;
        int cteParameterCount = 0;
        boolean cteMode = false;
        QuoteMode mode = QuoteMode.NONE;

        for (int i = 0; i < originalSql.length(); i++) {
            final char c = originalSql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
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
            }

            sb.append(c);
        }

        return cteParameterCount;
    }

    public static int countSelectParameters(String originalSql, int startIndex) {
        int brackets = 0;
        int selectParameterCount = 0;
        QuoteMode mode = QuoteMode.NONE;

        for (int i = startIndex; i < originalSql.length(); i++) {
            final char c = originalSql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    brackets++;
                } else if (c == ')') {
                    brackets--;
                } else if (c == '?') {
                    selectParameterCount++;
                }

                if (brackets == 0 && originalSql.regionMatches(true, i, "from ", 0, "from ".length())) {
                    break;
                }
            }
        }

        return selectParameterCount;
    }

    public static String getSubselectQueryForHibernatePre5(String subselectQuery) {
        // Hibernate before 5 couldn't find the correct from clause
        StringBuilder sb = null;
        int parens = 0;

        for (int i = 0; i < subselectQuery.length(); i++) {
            final char c = subselectQuery.charAt(i);
            if (c == '(') {
                parens++;
            } else if (c == ')') {
                parens--;
                if (parens < 0) {
                    // This is the case when we have a CTE
                    int fromIndex = subselectQuery.indexOf(" from ");
                    int startIndex = i;
                    int otherFromIndex;
                    do {
                        otherFromIndex = subselectQuery.indexOf(" from ", startIndex);
                        parens = 0;
                        for (int j = i + 1; j < otherFromIndex; j++) {
                            final char c2 = subselectQuery.charAt(j);
                            if (c2 == '(') {
                                parens++;
                            } else if (c2 == ')') {
                                parens--;
                            }
                        }
                        startIndex = otherFromIndex + 1;
                    } while (parens != 0);
                    sb = new StringBuilder(fromIndex + (subselectQuery.length() - otherFromIndex));
                    sb.append(subselectQuery, 0, fromIndex);
                    sb.append(subselectQuery, otherFromIndex, subselectQuery.length());
                    break;
                }
            }
        }

        if (sb != null) {
            subselectQuery = sb.toString();
        }
        return subselectQuery;
    }

}
