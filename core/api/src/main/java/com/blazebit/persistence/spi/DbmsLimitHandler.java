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
    boolean supportsLimit();

    /**
     * Returns whether the dbms supports OFFSET via SQL.
     *
     * @return True if OFFSET is supported, otherwise false
     */
    boolean supportsLimitOffset();

    /**
     * Returns whether the dbms supports parameters for LIMIT and OFFSET via prepared statements.
     *
     * @return True if parameters allowed, otherwise false
     */
    public boolean supportsVariableLimit();

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
    String applySql(String sql, boolean isSubquery, Integer limit, Integer offset);

    /**
     * Like {@link DbmsLimitHandler#applySql(String, boolean, Integer, Integer)} but inlines the parameter values.
     *
     * @param sql    the sql query on which to apply
     * @param isSubquery whether the query is a subquery
     * @param limit  the limit or null
     * @param offset the offset or null
     * @return Query statement with LIMIT clause applied.
     */
    String applySqlInlined(String sql, boolean isSubquery, Integer limit, Integer offset);

    /**
     * Apply the LIMIT and OFFSET clause on the given SQL
     *
     * @param sqlSb  the string builder containing the sql query on which to apply
     * @param isSubquery whether the query is a subquery
     * @param limit  the limit value or null
     * @param offset the offset value or null
     * @return Query statement with LIMIT clause applied.
     */
    void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset);

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
    int bindLimitParametersAtStartOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException;

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
    int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException;

    /**
     * Use JDBC API to limit the number of rows returned by the SQL query. Typically handlers that do not
     * support LIMIT clause should implement this method.
     *
     * @param limit  the limit or null
     * @param offset the offset or null
     * @param statement the statement on which to apply max rows
     * @throws SQLException Indicates problems while limiting maximum rows returned
     */
    void setMaxRows(Integer limit, Integer offset, PreparedStatement statement) throws SQLException;
}
