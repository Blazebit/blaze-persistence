/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import com.blazebit.persistence.ReturningResult;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

/**
 * Interface implemented by the criteria provider.
 *
 * It is invoked to do some extended functionality like retrieving sql and executing statements with custom sql.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ExtendedQuerySupport {

    /**
     * Returns whether the JPA provider supports advanced sql queries that need every method of this interface to work properly.
     *
     * @return Whether advanced sql queries are supported
     */
    public boolean supportsAdvancedSql();

    /**
     * Returns whether the JPA provider needs an example query for advanced sql DML queries.
     *
     * @return Whether advanced sql DML queries need an example query
     * @since 1.5.0
     */
    public boolean needsExampleQueryForAdvancedDml();

    /**
     * Applies the first and max results to the query.
     *
     * @param query The query to apply the first and max results
     * @param firstResult The first result to apply
     * @param maxResults The max results to apply
     * @return Whether firstResult or maxResult was set or unset whereas before it wasn't
     * @since 1.6.7
     */
    public boolean applyFirstResultMaxResults(Query query, int firstResult, int maxResults);

    /**
     * Returns the SQL query for the given query object.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @return The SQL query
     */
    public String getSql(EntityManager em, Query query);

    /**
     * Returns whether the return of {@link #getSql(EntityManager, Query)} will also contain the limit/offset in the SQL.
     *
     * @return Whether {@link #getSql(EntityManager, Query)} contains the limit/offset
     * @since 1.6.7
     */
    public boolean getSqlContainsLimit();

    /**
     * Returns the cascading SQL delete queries for the given query object.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @return The cascading SQL delete queries
     */
    public List<String> getCascadingDeleteSql(EntityManager em, Query query);

    /**
     * Returns the SQL table alias of the JPQL from node alias in the given query.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @param alias The from node alias
     * @param queryPartNumber The 0-based query part number
     * @return The SQL table alias
     */
    public String getSqlAlias(EntityManager em, Query query, String alias, int queryPartNumber);

    /**
     * Returns the SQL table alias of the JPQL from node alias in the given query.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @param alias The from node alias
     * @param queryPartNumber The 0-based query part number
     * @return The SQL table alias position in the SQL string
     * @since 1.6.7
     */
    public SqlFromInfo getSqlFromInfo(EntityManager em, Query query, String alias, int queryPartNumber);

    // TODO: adapt to return (position, alias, expression) instead
    /**
     * Returns the corresponding position of the given JPQL select alias in the SQL query's select clause of the given query object.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @param alias The JPQL select alias
     * @return The position of the corresponding SQL select clause item
     */
    public int getSqlSelectAliasPosition(EntityManager em, Query query, String alias);

    /**
     * Returns the corresponding position of the given JPQL attribute expression in the SQL query's select clause of the given query object.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @param attribute The JPQL attribute expression
     * @return The position of the corresponding SQL select clause item
     */
    public int getSqlSelectAttributePosition(EntityManager em, Query query, String attribute);

    /**
     * Returns the result list of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @param queryPlanCacheEnabled Designates whether query plans can be cached and reused
     * @return The result of the query
     */
    @SuppressWarnings("rawtypes")
    public List getResultList(ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled);

    /**
     * Returns the result stream of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @param queryPlanCacheEnabled Designates whether query plans can be cached and reused
     * @return The result of the query
     * @since 1.6.2
     */
    public Object getResultStream(ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled);

    /**
     * Returns the single result of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @param queryPlanCacheEnabled Designates whether query plans can be cached and reused
     * @return The result of the query
     */
    public Object getSingleResult(ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride, boolean queryPlanCacheEnabled);

    /**
     * Executes and returns the update count of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param baseQuery The base query which represents the original modification query
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @param queryPlanCacheEnabled Designates whether query plans can be cached and reused
     * @return The update count of the query
     */
    public int executeUpdate(ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query query, String sqlOverride, boolean queryPlanCacheEnabled);

    /**
     * Executes and returns the returning result of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param baseQuery The base query which represents the original modification query
     * @param exampleQuery The example query providing the result type structure
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @param queryPlanCacheEnabled Designates whether query plans can be cached and reused
     * @return The returning result of the query
     */
    public ReturningResult<Object[]> executeReturning(ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query exampleQuery, String sqlOverride, boolean queryPlanCacheEnabled);

    /**
     * Provides SQL information about a FROM element.
     *
     * @author Christian Beikov
     * @since 1.6.7
     */
    public static interface SqlFromInfo {
        /**
         * Returns the table alias.
         *
         * @return the table alias
         */
        String getAlias();
        /**
         * Returns the from element start index in the SQL.
         *
         * @return the from element start index
         */
        int getFromStartIndex();
        /**
         * Returns the from element end index in the SQL.
         *
         * @return the from element end index
         */
        int getFromEndIndex();
    }
}
