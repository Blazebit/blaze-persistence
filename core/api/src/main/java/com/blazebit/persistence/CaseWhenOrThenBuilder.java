/*
 * Copyright 2014 - 2022 Blazebit.
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
 * The builder interface for a when predicate container that connects predicates with the OR operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CaseWhenOrThenBuilder<T extends CaseWhenBuilder<?>> extends CaseWhenThenBuilder<T> {

    /**
     * Starts a {@link RestrictionBuilder} for a case when predicate with the given expression as left hand expression.
     * When the builder finishes, the predicate is added this predicate container.
     *
     * @param expression The left hand expression for a case when predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<CaseWhenOrThenBuilder<T>> or(String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to this predicate container as disjunct.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery();

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the when predicate in conjunction with it's then
     * expression are added to this predicate container as disjunct.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     *
     * @see CaseWhenStarterBuilder#whenSubquery(java.lang.String, java.lang.String) More details about this method
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery(String subqueryAlias, String expression);
    
    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used for the left hand side of a when predicate.
     * </p>
     * 
     * @param expression The expression which will be used as left hand side of a when predicate
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a when predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to this predicate container as disjunct.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a when predicate.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the when predicate in conjunction with it's then
     * expression are added to this predicate container as disjunct.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     *
     * @see CaseWhenStarterBuilder#whenSubquery(java.lang.String, java.lang.String) More details about this method
     */
    public SubqueryBuilder<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to this predicate container as
     * disjunct.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenOrThenBuilder<T>> orExists();

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to this predicate container as
     * disjunct.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenOrThenBuilder<T>> orNotExists();

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to this predicate container as
     * disjunct.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<CaseWhenOrThenBuilder<T>> orExists(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to this predicate container as
     * disjunct.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<CaseWhenOrThenBuilder<T>> orNotExists(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a case when and builder which connects it's predicates with the AND operator.
     * When the builder finishes, the predicate is added to this predicate container as disjunct.
     *
     * @return The case when and builder
     */
    public CaseWhenAndBuilder<CaseWhenOrThenBuilder<T>> and();
}
