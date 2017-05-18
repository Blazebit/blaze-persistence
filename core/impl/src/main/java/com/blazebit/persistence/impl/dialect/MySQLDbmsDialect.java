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

package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.ValuesStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class MySQLDbmsDialect extends DefaultDbmsDialect {

    public MySQLDbmsDialect() {
        super(getSqlTypes());
    }

    public MySQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    protected static Map<Class<?>, String> getSqlTypes() {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        types.put(String.class, "longtext");

        return types;
    }

    @Override
    public boolean supportsWithClause() {
        return false;
    }

    @Override
    public boolean supportsNonRecursiveWithClause() {
        return false;
    }

    @Override
    public String getWithClause(boolean recursive) {
        throw new UnsupportedOperationException("With clause is not supported!");
    }

    @Override
    public boolean supportsUnion(boolean all) {
        return true;
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        return false;
    }

    @Override
    public boolean supportsExcept(boolean all) {
        return false;
    }

    @Override
    public boolean supportsGroupByExpressionInHavingMatching() {
        // MySQL re-evaluates all expressions in the having clause which is why it needs access to all column values
        return false;
    }

    @Override
    public boolean supportsFullRowValueComparison() {
        // MySQL can correctly evaluate row value comparisons but only uses them as filter predicate when accessing the index.
        // http://use-the-index-luke.com/de/sql/partielle-ergebnisse/blaettern
        return true;
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new MySQLDbmsLimitHandler();
    }

    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (!element.isNullable()) {
            super.appendOrderByElement(sqlSb, element, aliases);
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element, aliases);
        }
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return ValuesStrategy.SELECT_UNION;
    }

    @Override
    public boolean needsCastParameters() {
        return false;
    }

    @Override
    protected boolean needsAliasForFromClause() {
        return true;
    }
}
