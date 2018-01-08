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

package com.blazebit.persistence.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handler for applying SQL LIMIT and OFFSET to a query.
 *
 * Similar to Hibernates LimitHandler interface.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DbmsLimitHandler {
    /**
     * Returns whether the dbms supports LIMIT via SQL.
     *
     * @return True if LIMIT is supported, otherwise false
     */
    public boolean supportsLimit();

    /**
     * Returns whether the dbms supports OFFSET via SQL.
     *
     * @return True if OFFSET is supported, otherwise false
     */
    public boolean supportsLimitOffset();

    /**
     * Returns whether the parameter value for OFFSET should be added to the value of LIMIT.
     *
     * @return True if OFFSET should be added to LIMIT, otherwise false
     */
    public boolean limitIncludesOffset();

    /**
     * Apply the LIMIT and OFFSET clause on the given SQL as parameters.
     * If parameters are not supported by the DBMS the values should be inlined.
     *
     * @param sql    the sql query on which to apply
     * @param isSubquery whether the query is a subquery
     * @param limit  the limit or null
     * @param offset the offset or null
     * @return Query statement with LIMIT clause applied.
     */
    public String applySql(String sql, boolean isSubquery, Integer limit, Integer offset);

    /**
     * Like {@link DbmsLimitHandler#applySql(String, boolean, Integer, Integer)} but inlines the parameter values.
     *
     * @param sql    the sql query on which to apply
     * @param isSubquery whether the query is a subquery
     * @param limit  the limit or null
     * @param offset the offset or null
     * @return Query statement with LIMIT clause applied.
     */
    public String applySqlInlined(String sql, boolean isSubquery, Integer limit, Integer offset);

    /**
     * Apply the LIMIT and OFFSET clause on the given SQL.
     *
     * @param sqlSb  the string builder containing the sql query on which to apply
     * @param isSubquery whether the query is a subquery
     * @param limit  the limit value or null
     * @param offset the offset value or null
     */
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset);

    /**
     * Bind parameter values needed by the LIMIT clause before original SELECT statement.
     *
     * @param limit  the limit or null
     * @param offset the offset or null
     * @param statement the statement to which to apply parameters
     * @param index the index on which to bind parameters
     * @return The number of parameter values bound
     * @throws SQLException Indicates problems binding parameter values
     */
    public int bindLimitParametersAtStartOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException;

    /**
     * Bind parameter values needed by the LIMIT clause after original SELECT statement.
     *
     * @param limit  the limit or null
     * @param offset the offset or null
     * @param statement the statement to which to apply parameters
     * @param index the index on which to bind parameters
     * @return The number of parameter values bound
     * @throws SQLException Indicates problems binding parameter values
     */
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException;

    /**
     * Use JDBC API to limit the number of rows returned by the SQL query. Typically handlers that do not
     * support LIMIT clause should implement this method.
     *
     * @param limit  the limit or null
     * @param offset the offset or null
     * @param statement the statement on which to apply max rows
     * @throws SQLException Indicates problems while limiting maximum rows returned
     */
    public void setMaxRows(Integer limit, Integer offset, PreparedStatement statement) throws SQLException;
}
