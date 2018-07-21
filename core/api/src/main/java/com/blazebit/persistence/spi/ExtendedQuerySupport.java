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

import com.blazebit.persistence.ReturningResult;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
     * Returns the SQL query for the given query object.
     *
     * @param em The entity manager the query is associated to
     * @param query The JPA query
     * @return The SQL query
     */
    public String getSql(EntityManager em, Query query);

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
     * @return The SQL table alias
     */
    public String getSqlAlias(EntityManager em, Query query, String alias);

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
     * @return The result of the query
     */
    @SuppressWarnings("rawtypes")
    public List getResultList(ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride);

    /**
     * Returns the single result of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @return The result of the query
     */
    public Object getSingleResult(ServiceProvider serviceProvider, List<Query> participatingQueries, Query query, String sqlOverride);

    /**
     * Executes and returns the update count of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param baseQuery The base query which represents the original modification query
     * @param query The main query to execute
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @return The update count of the query
     */
    public int executeUpdate(ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query query, String sqlOverride);

    /**
     * Executes and returns the returning result of the Query by replacing the SQL with the given overriding SQL query.
     *
     * @param serviceProvider The service provider to access {@linkplain EntityManager} and others
     * @param participatingQueries The list of participating queries from which to combine parameters
     * @param baseQuery The base query which represents the original modification query
     * @param exampleQuery The example query providing the result type structure
     * @param sqlOverride The actual SQL query to execute instead of the query's original SQL
     * @return The returning result of the query
     */
    public ReturningResult<Object[]> executeReturning(ServiceProvider serviceProvider, List<Query> participatingQueries, Query baseQuery, Query exampleQuery, String sqlOverride);
}
