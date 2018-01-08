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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;



/**
 * Interface for implementing some dbms specifics.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 * @see CriteriaBuilderConfiguration#registerDialect(java.lang.String, com.blazebit.persistence.spi.DbmsDialect)
 */
public interface DbmsDialect {

    /* With clause handling */
    
    /**
     * Returns true if the dbms supports the with clause, false otherwise.
     * 
     * @return Whether the with clause is supported by the dbms
     */
    public boolean supportsWithClause();
    
    /**
     * Returns true if the dbms supports the non-recursive with clause, false otherwise.
     * 
     * @return Whether the non-recursive with clause is supported by the dbms
     */
    public boolean supportsNonRecursiveWithClause();

    /**
     * Returns true if the dbms supports the with clause head for aliasing, false otherwise.
     *
     * @return Whether the with clause head is supported by the dbms
     */
    public boolean supportsWithClauseHead();

    /**
     * Returns the SQL representation for the normal or recursive with clause. 
     * 
     * @param recursive Whether the clause should be able to contain recursive queries or not
     * @return The with clause name
     */
    public String getWithClause(boolean recursive);

    /**
     * Appends the with clause to the sql string builder.
     * 
     * @param sqlSb The sql string builder to which the with clause should be append to
     * @param statementType The type of the statement in the sql string builder
     * @param isSubquery True if the query in the sql string builder is a subquery, false otherwise
     * @param isEmbedded True if the query in the sql string builder will be embedded in a clause, false otherwise
     * @param withClause The with clause which should be appended, or null if none
     * @param limit The limit for the limit clause, or null if no limit
     * @param offset The offset for the offset clause, or null if no offset
     * @param returningColumns The columns which the sql should return or null if none
     * @param includedModificationStates The modification states of the returned columns for which additional CTEs should be generated mapped to the expected CTE names
     * @return Generated CTEs queries for the requested modification states
     */
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates);

    /**
     * Connects the given operands with the given set operation and appends that to the sql string builder.
     * 
     * @param sqlSb The sql string builder to which the connected operands should be appended to
     * @param setType The type of the set connection
     * @param isSubquery True if the query in the sql string builder is a subquery, false otherwise
     * @param operands An list of operand sql strings
     * @param orderByElements The order by elements of the composite set operation
     * @param limit The limit for the limit clause, or null if no limit
     * @param offset The offset for the offset clause, or null if no offset
     */
    public void appendSet(StringBuilder sqlSb, SetOperationType setType, boolean isSubquery, List<String> operands, List<? extends OrderByElement> orderByElements, String limit, String offset);

    /**
     * Creates a new limit handler for an SQL query.
     *
     * @return A new limit handler for a query
     * @since 1.2.0
     */
    public DbmsLimitHandler createLimitHandler();
    
    /**
     * Returns true if the dbms supports the with clause in modification queries, false otherwise.
     * 
     * @return Whether the with clause is supported in modification queries by the dbms
     */
    public boolean supportsWithClauseInModificationQuery();

    /**
     * Returns true if the dbms supports modification queries in the with clause, false otherwise.
     * 
     * @return Whether modification queries are supported in the with clause by the dbms
     */
    public boolean supportsModificationQueryInWithClause();
    
    /**
     * Returns true if the dbms wants to use the JDBC executeUpdate method when using the with clause in modification queries, false otherwise.
     * 
     * @return Whether the JDBC executeUpdate method should be used when using the with clause is in modification queries
     */
    public boolean usesExecuteUpdateWhenWithClauseInModificationQuery();
    
    /* Returning clause handling */

    /**
     * Returns true if the dbms supports returning generated keys, false otherwise.
     * 
     * @return Whether returning generated keys is supported by the dbms
     */
    public boolean supportsReturningGeneratedKeys();
    
    /**
     * Returns true if the dbms supports returning all generated keys, false otherwise.
     * 
     * @return Whether returning all generated keys is supported by the dbms
     */
    public boolean supportsReturningAllGeneratedKeys();

    /**
     * Returns true if the dbms supports returning columns from a modified row, false otherwise.
     * 
     * @return Whether returning columns from a modified row is supported by the dbms
     */
    public boolean supportsReturningColumns();

    /**
     * Returns true if the dbms supports complex expressions like subqueries or parameters as group by elements, false otherwise.
     * 
     * @return Whether complex group by elements are supported by the dbms
     * @since 1.1.0
     */
    public boolean supportsComplexGroupBy();

    /**
     * Returns true if the dbms supports matching non-trivial expressions that appear in the group by clause with usages in the having clause.
     *
     * @return Whether expressions from the group by clause are matched and reused in the having clause by the dbms
     * @since 1.2.0
     */
    public boolean supportsGroupByExpressionInHavingMatching();

    /**
     * Returns true if the dbms supports complex expressions like subqueries in the join on clause, false otherwise.
     *
     * @return Whether complex join on clauses are supported by the dbms
     * @since 1.2.0
     */
    public boolean supportsComplexJoinOn();
    
    /**
     * Returns true if the dbms supports the set operation UNION, false otherwise.
     * 
     * @param all True if the non-distinct ALL operation should be checked, false otherwise.
     * @return Whether UNION is supported by the dbms
     * @since 1.1.0
     */
    public boolean supportsUnion(boolean all);

    /**
     * Returns true if the dbms supports the set operation INTERSECT, false otherwise.
     * 
     * @param all True if the non-distinct ALL operation should be checked, false otherwise.
     * @return Whether INTERSECT is supported by the dbms
     * @since 1.1.0
     */
    public boolean supportsIntersect(boolean all);

    /**
     * Returns true if the dbms supports the set operation EXCEPT, false otherwise.
     * 
     * @param all True if the non-distinct ALL operation should be checked, false otherwise.
     * @return Whether EXCEPT is supported by the dbms
     * @since 1.1.0
     */
    public boolean supportsExcept(boolean all);

    /**
     * Returns true if the dbms supports joins in the recursive part of a CTE, false otherwise.
     * 
     * @return Whether joins are supported in recursive CTEs by the dbms
     * @since 1.2.0
     */
    public boolean supportsJoinsInRecursiveCte();

    /**
     * Returns true if the dbms supports row value constructor syntax, false otherwise.
     *
     * @return Whether row value constructor syntax is supported by the dbms
     * @since 1.2.0
     */
    public boolean supportsRowValueConstructor();

    /**
     * Returns true if the dbms supports all <, <=, >, >=, =, <> comparison operations for row values.
     * Note that some DBMS only support = and <> operators.
     *
     * @return Whether full row value comparison operations are supported by the dbms
     * @since 1.2.0
     */
    public boolean supportsFullRowValueComparison();

    /**
     * Returns the sql type for the java class type for usage in cast expressions.
     *
     * @param castType The java class type
     * @return The sql type
     * @since 1.2.0
     */
    public String getSqlType(Class<?> castType);

    /**
     * Returns the strategy to use for values generation.
     *
     * @return The VALUES strategy
     * @since 1.2.0
     */
    public ValuesStrategy getValuesStrategy();

    /**
     * Returns whether parameters need to be casted if occurring in an untyped context like e.g. VALUES clause.
     *
     * @return True if casting is required, otherwise false
     * @since 1.2.0
     */
    public boolean needsCastParameters();

    /**
     * Returns the name of a dummy table like DUAL in Oracle or null if none is required.
     *
     * @return The dummy table name or null
     * @since 1.2.0
     */
    public String getDummyTable();

    /**
     * Returns the cast expression for the given expression to the given sql type.
     *
     * @param expression The expression to cast
     * @param sqlType The type to which to cast
     * @return The cast expression
     * @since 1.2.0
     */
    public String cast(String expression, String sqlType);

    /**
     * Returns whether sql types for the returning columns need to be provided.
     *
     * @return True if sql types are required, otherwise false
     * @since 1.2.0
     */
    public boolean needsReturningSqlTypes();

    /**
     * TODO: documentation.
     *
     * @return The prepare flags
     * @since 1.2.0
     */
    public int getPrepareFlags();

    /**
     * TODO: documentation.
     *
     * @param ps TODO: documentation
     * @param returningSqlTypes TODO: documentation
     * @return The prepared statement
     * @throws SQLException When preparing the statement fails
     * @since 1.2.0
     */
    public PreparedStatement prepare(PreparedStatement ps, int[] returningSqlTypes) throws SQLException;

    /**
     * TODO: documentation.
     *
     * @param ps TODO: documentation
     * @return The result set for the returning clause
     * @throws SQLException When extracting from the statement fails
     * @since 1.2.0
     */
    public ResultSet extractReturningResult(PreparedStatement ps) throws SQLException;
}
