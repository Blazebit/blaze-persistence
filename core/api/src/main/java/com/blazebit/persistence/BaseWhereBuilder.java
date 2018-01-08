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
 * A base interface for builders that support filtering.
 * This is related to the fact, that a query builder supports where clauses.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface BaseWhereBuilder<T extends BaseWhereBuilder<T>> {

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<T>> whereSubquery();

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a predicate.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<T>> whereSubquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for the left hand side of a predicate.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used for the left hand side of the predicate.
     * </p>
     * 
     * @param expression The expression which will be used as left hand side of a predicate
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<RestrictionBuilder<T>> whereSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<RestrictionBuilder<T>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a predicate. All occurrences of <code>subqueryAlias</code> in
     * <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type <code>T</code>.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<RestrictionBuilder<T>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link RestrictionBuilder} for a where predicate with the given expression as left hand expression.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param expression The left hand expression for a where predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<T> where(String expression);

    /**
     * Starts a {@link CaseWhenBuilder} for a where predicate.
     * When the {@link CaseWhenBuilder} and the restriction builder for the right hand side are finished,
     * the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return A {@link CaseWhenBuilder}
     */
    public CaseWhenStarterBuilder<RestrictionBuilder<T>> whereCase();

    /**
     * Starts a {@link SimpleCaseWhenBuilder} for a where predicate.
     * When the {@link CaseWhenBuilder} and the restriction builder for the right hand side are finished,
     * the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param expression Case operand expression
     * @return A {@link CaseWhenBuilder}
     */
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<T>> whereSimpleCase(String expression);

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> whereExists();

    /**
     * Starts an not exists predicate for the where clause with a subquery on the right hand side.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> whereNotExists();

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> whereExists(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts an exists predicate for the where clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder);
}
