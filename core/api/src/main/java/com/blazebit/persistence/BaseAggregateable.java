/*
 * Copyright 2014 Blazebit.
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
 * A base interface for builders that support aggregation.
 * This is related to the fact, that a query builder supports group by and having clauses.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.0
 */
public interface BaseAggregateable<T extends BaseAggregateable<T>> {
    
    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    // TODO: rename to havingSubquery
    public SubqueryInitiator<RestrictionBuilder<T>> having();
    
    // TODO: add SubqueryInitiator<RestrictionBuilder<T>> havingSubquery(String expression, String subqueryAlias)

    /**
     * Starts a {@link RestrictionBuilder} for a having predicate with the given expression as left hand expression.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @param expression The left hand expression for a having predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<T> having(String expression);

    /**
     * Starts an exists predicate for the having clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> havingExists();
    
    /**
     * Starts an exists predicate for the having clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> havingNotExists();
}
