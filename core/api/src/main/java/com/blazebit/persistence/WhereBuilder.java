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

/**
 * An interface for builders that support filtering. This is related to the
 * fact, that a query builder supports where clauses.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface WhereBuilder<T extends WhereBuilder<T>> extends BaseWhereBuilder<T> {

    /**
     * Starts a {@link WhereOrBuilder} which is a predicate consisting only of
     * disjunctiv connected predicates. When the builder finishes, the predicate
     * is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The or predicate builder for the where clause
     */
    public WhereOrBuilder<T> whereOr();
    
    /**
     * Sets the given expression as expression for the where clause.
     *  
     * @param expression The where expression
     * @return The builder
     * @since 1.2.0
     */
    public T setWhereExpression(String expression);
    
    /**
     * Starts a {@link MultipleSubqueryInitiator} for expression of the where clause.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used as expression for the where clause.
     * </p>
     *  
     * @param expression The where expression
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> setWhereExpressionSubqueries(String expression);
}
