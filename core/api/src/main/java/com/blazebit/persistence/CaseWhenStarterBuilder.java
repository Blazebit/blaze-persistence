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
 * A builder for general case when expressions.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CaseWhenStarterBuilder<T> {

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
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate. All occurrences of <code>subqueryAlias</code> in
     * <code>expression</code> will be replaced by the subquery. This allows to build
     * expressions containing subqueries like following example shows:
     * 
     * <p>
     * {@code whenSubquery("x", "x * 2 + 1").from(Person.class, "p").select("COUNT(p)").end(); }
     * </p>
     * 
     * <p>
     * results in:
     * </p>
     * 
     * <p>
     * {@code (SELECT COUNT(p) FROM Person p) * 2 + 1}
     * </p>
     * 
     * <p>
     * When the subquery builder and the restriction builder for the right hand side are finished, the when predicate in conjunction
     * with it's then expression are added to the case when builder.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression);
    
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
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a when predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the left hand side of a when predicate. All occurrences of <code>subqueryAlias</code> in
     * <code>expression</code> will be replaced by the subquery. This allows to build
     * expressions containing subqueries like following example shows:
     *
     * <p>
     * {@code whenSubquery("x", "x * 2 + 1").from(Person.class, "p").select("COUNT(p)").end(); }
     * </p>
     *
     * <p>
     * results in:
     * </p>
     *
     * <p>
     * {@code (SELECT COUNT(p) FROM Person p) * 2 + 1}
     * </p>
     *
     * <p>
     * When the subquery builder and the restriction builder for the right hand side are finished, the when predicate in conjunction
     * with it's then expression are added to the case when builder.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

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
     * Starts an exists predicate for the when clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenExists(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side based on the given criteria builder.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenNotExists(FullQueryBuilder<?, ?> criteriaBuilder);

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
}
