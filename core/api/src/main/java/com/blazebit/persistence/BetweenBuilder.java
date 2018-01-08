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
 * The builder interface for between predicates.
 * 
 * <p>
 * This builder allows the specification of the upper bound of the between predicate. The left hand side expression of the between
 * predicate as well as the lower bound expression are already known to the builder and the methods of this builder either terminate the
 * building process or start a {@link SubqueryInitiator}.
 * </p>
 * 
 * @param <T> The builder type that is returned on terminal operations
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface BetweenBuilder<T> {

    /**
     * Constructs a between predicate with a parameter as upper bound.
     * 
     * @param end The parameter for the upper bound
     * @return The parent predicate container builder
     */
    public T and(Object end);

    /**
     * Constructs a between predicate with an expression as upper bound.
     * 
     * @param end The upper bound expression
     * @return The parent predicate container builder
     */
    public T andExpression(String end);

    /**
     * Constructs a between predicate with a subquery as upper bound.
     * 
     * @return The {@link SubqueryInitiator} for building the upper bound subquery.
     */
    public SubqueryInitiator<T> andSubqery();

    /**
     * Constructs a between predicate with an expression containing a subquery as upper bound.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. This allows to build
     * expressions containing subqueries like following example shows:
     * </p>
     * 
     * <p>
     * {@code andSubquery("x", "x * 2 + 1").from(Person.class, "p").select("COUNT(p)").end(); }
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
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * 
     * @return The {@link SubqueryInitiator} for building the upper bound subquery.
     */
    public SubqueryInitiator<T> andSubqery(String subqueryAlias, String expression);
    
    /**
     * Constructs a between predicate with an expression containing the given expression as upper bound.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the resulting expression is used for the upper bound of the between predicate.
     * </p>
     * 
     * @param expression The expression which will be used as upper bound of a the between predicate
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> andSubqueries(String expression);

    /**
     * Constructs a between predicate with a subquery based on the given criteria builder as upper bound.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building the upper bound subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> andSubqery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Constructs a between predicate with an expression containing a subquery as upper bound.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. This allows to build
     * expressions containing subqueries like following example shows:
     * </p>
     *
     * <p>
     * {@code andSubquery("x", "x * 2 + 1").from(Person.class, "p").select("COUNT(p)").end(); }
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
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building the upper bound subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> andSubqery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);
}
