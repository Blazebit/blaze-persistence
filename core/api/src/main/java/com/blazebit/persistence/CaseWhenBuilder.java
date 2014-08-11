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
 * A builder for general case when expressions.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface CaseWhenBuilder<T> {

    /**
     * Starts a {@link RestrictionBuilder} to create a when predicate where expression will be on the left hand side of the predicate.
     *
     * @param expression The left hand expression for a when predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> when(String expression);
    
    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery();

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate. All occurrences of
     * <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression);

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenExists();

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenNotExists();

    /**
     * Starts a {@link CaseWhenAndThenBuilder} which is a predicate consisting only of
     * conjunctive connected predicates. When the builder finishes, the when predicate
     * in conjunction with it's then expression are added to the case when builder.
     *
     * @return The and predicate builder for the when expression
     */
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> whenAnd();

    /**
     * Starts a {@link CaseWhenOrThenBuilder} which is a predicate consisting only of
     * disjunctiv connected predicates. When the builder finishes, the when predicate
     * in conjunction with it's then expression are added to the case when builder.
     *
     * @return The or predicate builder for the when expression
     */
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> whenOr();

    /**
     * Adds the given else expression to the case when builder.
     *
     * @param elseExpression The else expression
     * @return The parent builder
     */
    public T thenElse(String elseExpression);
}
