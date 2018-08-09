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

package com.blazebit.persistence;

import javax.persistence.TypedQuery;
import java.lang.reflect.Constructor;

/**
 * A base interface for builders that support normal query functionality.
 * This interface is shared between the criteria builder and paginated criteria builder.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FullQueryBuilder<T, X extends FullQueryBuilder<T, X>> extends QueryBuilder<T, X>, FetchBuilder<X> {

    /**
     * Copies this query builder into a new one, using it's projection as an overridable default.
     *
     * @param resultClass The result class of the query
     * @param <Y> The type of the result class
     * @return A new query builder
     * @since 1.2.0
     */
    public <Y> FullQueryBuilder<Y, ?> copy(Class<Y> resultClass);

    /**
     * Returns a query that counts the results that would be produced if the current query was run.
     *
     * @return A query for determining the count of the result list represented by this query builder
     * @since 1.2.0
     */
    public TypedQuery<Long> getCountQuery();

    /**
     * Returns the query string that selects the count of elements.
     *
     * @return The query string
     * @since 1.3.0
     */
    public String getCountQueryString();

    /**
     * Invokes {@link FullQueryBuilder#pageBy(int, int, String, String...)} with the identifiers of the query root entity.
     *
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @return This query builder as paginated query builder
     */
    public PaginatedCriteriaBuilder<T> page(int firstResult, int maxResults);

    /**
     * Invokes {@link FullQueryBuilder#pageByAndNavigate(Object, int, String, String...)} with the identifiers of the query root entity.
     *
     * @deprecated This method causes a method resolution ambiguity. Use {{@link #pageAndNavigate(Object, int)}} instead.
     * @param entityId The id of the entity which should be located on the page
     * @param maxResults The maximum number of results to retrieve
     * @return This query builder as paginated query builder
     */
    @Deprecated
    public PaginatedCriteriaBuilder<T> page(Object entityId, int maxResults);

    /**
     * Invokes {@link FullQueryBuilder#pageByAndNavigate(Object, int, String, String...)} with the identifiers of the query root entity.
     *
     * @param entityId The id of the entity which should be located on the page
     * @param maxResults The maximum number of results to retrieve
     * @return This query builder as paginated query builder
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> pageAndNavigate(Object entityId, int maxResults);

    /**
     * Invokes {@link FullQueryBuilder#pageBy(KeysetPage, int, int, String, String...)} with the identifiers of the query root entity.
     *
     * @param keysetPage The key set from a previous result, may be null
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @return This query builder as paginated query builder
     * @see PagedList#getKeysetPage()
     */
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstResult, int maxResults);

    /**
     * Like {@link FullQueryBuilder#pageBy(int, int, String, String...)} but lacks the varargs parameter to avoid heap pollution.
     *
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @return This query builder as paginated query builder
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> pageBy(int firstResult, int maxResults, String identifierExpression);

    /**
     * Like {@link FullQueryBuilder#pageByAndNavigate(Object, int, String, String...)} but lacks the varargs parameter to avoid heap pollution.
     *
     * @param entityId The id of the entity which should be located on the page
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @return This query builder as paginated query builder
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> pageByAndNavigate(Object entityId, int maxResults, String identifierExpression);

    /**
     * Like {@link FullQueryBuilder#pageBy(KeysetPage, int, int, String, String...)} but lacks the varargs parameter to avoid heap pollution.
     *
     * @param keysetPage The key set from a previous result, may be null
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @return This query builder as paginated query builder
     * @since 1.3.0
     * @see PagedList#getKeysetPage()
     */
    public PaginatedCriteriaBuilder<T> pageBy(KeysetPage keysetPage, int firstResult, int maxResults, String identifierExpression);

    /**
     * Paginates the results of this query based on the given identifier expressions.
     *
     * In JPA, the use of <code>setFirstResult</code> and <code>setMaxResults</code> is not defined when involving fetch joins for collections.
     * When no collection joins are involved, this is fine as rows essentially represent objects, but when collections are joined, this is no longer true.
     * JPA providers usually fall back to querying all data and doing pagination in-memory based on objects or simply don't support that kind of query.
     *
     * This API allows to specify the identifier expressions to use for pagination and transparently handles collection join support.
     * The big advantage of this API over plain <code>setFirstResult</code> and <code>setMaxResults</code> can also be seen when doing scalar queries.
     * 
     * <p>
     * An example for such queries would be a query that joins a collection:
     *
     * <code>SELECT d.id, contacts.name FROM Document d LEFT JOIN d.contacts contacts</code>
     *
     * If one <code>Document</code> has associated multiple contacts, the above query will produce multiple result set rows for that document.
     * Paginating via <code>setFirstResult</code> and <code>setMaxResults</code> would produce unexpected results whereas using this API, will produce the expected results.
     * </p>
     * 
     * <p>
     * When paginating on the identifier i.e. <code>d.id</code>, the results are implicitly grouped by the document id and distinct. Therefore calling
     * distinct() on a PaginatedCriteriaBuilder is not allowed.
     * </p>
     * 
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @param identifierExpressions The other identifier expressions
     * @return This query builder as paginated query builder
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> pageBy(int firstResult, int maxResults, String identifierExpression, String... identifierExpressions);

    /**
     * Paginates the results of this query and navigates to the page on which
     * the object with the given identifier is located.
     * 
     * Beware that the same limitations like for {@link FullQueryBuilder#page(int, int)} apply.
     * If the object with the given identifier does not exist in the result list:
     * <ul>
     * <li>The result of {@link PaginatedCriteriaBuilder#getResultList()} will contain the first page</li>
     * <li>{@link PagedList#getFirstResult()} will return <code>-1</code></li>
     * </ul>
     * 
     * @param entityId The id of the object which should be located on the page
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @param identifierExpressions The other identifier expressions
     * @return This query builder as paginated query builder
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> pageByAndNavigate(Object entityId, int maxResults, String identifierExpression, String... identifierExpressions);

    /**
     * Like {@link FullQueryBuilder#page(int, int)} but additionally uses key set pagination when possible.
     * 
     * Beware that keyset pagination should not be used as a direct replacement for offset pagination.
     * Since entries that have a lower rank than some keyset might be added or removed, the calculations
     * for the firstResult might be wrong. If strict pagination is required, then the {@link KeysetPage} should
     * not be used when the count of lower ranked items changes which will result in the use of offset pagination for that request.
     * 
     * <p>
     * Key set pagination is possible if and only if the following conditions are met:
     * <ul>
     * <li>The keyset reference values fit the order by expressions of this query builder AND</li>
     * <li>{@link KeysetPage#getMaxResults()} and <code>maxResults</code> evaluate to the same value AND</li>
     * <li>One of the following conditions is met:
     * <ul>
     * <li>The absolute value of {@link KeysetPage#getFirstResult()}<code> - firstResult</code> is 0</li>
     * <li>The absolute value of {@link KeysetPage#getFirstResult()}<code> - firstResult</code> is equal to the value of
     * <code>maxResults</code></li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param keysetPage The key set from a previous result, may be null
     * @param firstResult The position of the first result to retrieve, numbered from 0
     * @param maxResults The maximum number of results to retrieve
     * @param identifierExpression The first identifier expression
     * @param identifierExpressions The other identifier expressions
     * @return This query builder as paginated query builder
     * @since 1.3.0
     * @see PagedList#getKeysetPage()
     */
    public PaginatedCriteriaBuilder<T> pageBy(KeysetPage keysetPage, int firstResult, int maxResults, String identifierExpression, String... identifierExpressions);

    /*
     * Join methods
     */
    /**
     * Adds a join to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * If fetch is set to true, a join fetch will be added.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param fetch True if a join fetch should be added
     * @return The query builder for chaining calls
     */
    public X join(String path, String alias, JoinType type, boolean fetch);

    /**
     * Adds a join to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * The resulting join will be the default join meaning that expressions which use the absolute path will refer to this join.
     * If fetch is set to true, a join fetch will be added.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param fetch True if a join fetch should be added
     * @return The query builder for chaining calls
     */
    public X joinDefault(String path, String alias, JoinType type, boolean fetch);

    /**
     * Like {@link FullQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#INNER} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X innerJoinFetch(String path, String alias);

    /**
     * Like {@link FullQueryBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#INNER} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X innerJoinFetchDefault(String path, String alias);

    /**
     * Like {@link FullQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#LEFT} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X leftJoinFetch(String path, String alias);

    /**
     * Like {@link FullQueryBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#LEFT} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X leftJoinFetchDefault(String path, String alias);

    /**
     * Like {@link FullQueryBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#RIGHT} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X rightJoinFetch(String path, String alias);

    /**
     * Like {@link FullQueryBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType, boolean) } but with
     * {@link JoinType#RIGHT} and fetch set to true.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X rightJoinFetchDefault(String path, String alias);

    /*
     * Select methods
     */
    /**
     * Starts a {@link SelectObjectBuilder} for the given class. The types of the parameter arguments used in the
     * {@link SelectObjectBuilder} must match a constructor of the given class.
     *
     * @param <Y> The new query result type specified by the given class
     * @param clazz The class which should be used for the select new select clause
     * @return The select object builder for the given class
     */
    public <Y> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(Class<Y> clazz);

    /**
     * Starts a {@link SelectObjectBuilder} for the given constructor. The types of the parameter arguments used in the
     * {@link SelectObjectBuilder} must match the given constructor.
     *
     * @param <Y> The new query result type specified by the given class
     * @param constructor The constructor which should be used for the select new select clause
     * @return The select object builder for the given class
     */
    public <Y> SelectObjectBuilder<? extends FullQueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor);

    /**
     * Applies the given object builder to this query. The object builder provides the select clauses and is used to transform the
     * result set tuples.
     *
     * @param <Y> The new query result type specified by the given class
     * @param builder The object builder which transforms the result set into objects of type <code>Y</code>
     * @return The query builder for chaining calls
     */
    public <Y> FullQueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> builder);

}
