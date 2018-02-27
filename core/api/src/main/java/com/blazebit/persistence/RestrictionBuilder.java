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

import com.blazebit.persistence.internal.RestrictionBuilderExperimental;

import java.util.Collection;

/**
 * The builder interface for predicates.
 * The left hand side of the predicate is already known to the builder and the methods of this builder either terminate the building
 * process or start a {@link SubqueryInitiator}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface RestrictionBuilder<T> {

    /**
     * Starts a builder for a between predicate with lower bound expression.
     * 
     * @param start The between start expression
     * @return The {@link BetweenBuilder}
     */
    public BetweenBuilder<T> betweenExpression(String start);

    /**
     * Starts a builder for a between predicate with parameterized lower bound.
     * 
     * @param start The between start value
     * @return The {@link BetweenBuilder}
     */
    public BetweenBuilder<T> between(Object start);

    /**
     * Starts a builder for a between predicate with a subquery as lower bound.
     * 
     * @return The {@link SubqueryInitiator}
     */
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery();

    /**
     * Starts a builder for a between predicate with a subquery as lower bound.
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
     * @return The {@link SubqueryInitiator}
     */
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link MultipleSubqueryInitiator} for a between predicate with the given expression as lower bound.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the lower bound of the between predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<BetweenBuilder<T>> betweenSubqueries(String expression);

    /**
     * Starts a builder for a between predicate with a subquery as lower bound.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<BetweenBuilder<T>> betweenSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a builder for a between predicate with a subquery as lower bound.
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
     */
    public SubqueryBuilder<BetweenBuilder<T>> betweenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link RestrictionBuilder#betweenExpression(java.lang.String)} but the resulting predicate is negated.
     * 
     * @param start The between start expression
     * @return The {@link BetweenBuilder}
     */
    public BetweenBuilder<T> notBetweenExpression(String start);

    /**
     * Like {@link RestrictionBuilder#notBetween(java.lang.Object)} but the resulting predicate is negated.
     * 
     * @param start The between start value
     * @return The {@link BetweenBuilder}
     */
    public BetweenBuilder<T> notBetween(Object start);

    /**
     * Like {@link RestrictionBuilder#betweenSubquery()} but the resulting predicate is negated.
     * 
     * @return The {@link SubqueryInitiator}
     */
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery();

    /**
     * Like {@link RestrictionBuilder#betweenSubquery(java.lang.String, java.lang.String)} but the resulting predicate is negated.
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The {@link SubqueryInitiator}
     */
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery(String subqueryAlias, String expression);

    /**
     * Like {@link RestrictionBuilder#betweenSubqueries(java.lang.String)} but the resulting predicate is negated.
     * 
     * @param expression The expression for the lower bound of the between predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<BetweenBuilder<T>> notBetweenSubqueries(String expression);

    /**
     * Like {@link RestrictionBuilder#betweenSubquery(FullQueryBuilder)} but the resulting predicate is negated.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<BetweenBuilder<T>> notBetweenSubquery(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link RestrictionBuilder#betweenSubquery(java.lang.String, java.lang.String, FullQueryBuilder)} but the resulting predicate is negated.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<BetweenBuilder<T>> notBetweenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the EQ predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> eq();

    /**
     * Starts a {@link SubqueryInitiator} for the EQ predicate that can be used to apply quantors.
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
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> eq(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the EQ predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the right hand side of the EQ predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> eqSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the right hand side for the EQ predicate.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> eq(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the EQ predicate that can be used to apply quantors.
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
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> eq(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the EQ predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T eq(Object value);

    /**
     * Finishes the EQ predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T eqExpression(String expression);

    /**
     * Like {@link RestrictionBuilder#eq() } but the result is wrapped in a NOT predicate.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> notEq();

    /**
     * Like {@link RestrictionBuilder#eq(java.lang.String,java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate
     * @return The quantifiable binary predicate builder
     */
    public SubqueryInitiator<T> notEq(String subqueryAlias, String expression);

    /**
     * Like {@link RestrictionBuilder#eqSubqueries(java.lang.String) } but the result is wrapped in a NOT predicate.
     * 
     * @param expression The expression for the right hand side of the NOT EQ predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> notEqSubqueries(String expression);

    /**
     * Like {@link RestrictionBuilder#eq(FullQueryBuilder) } but the result is wrapped in a NOT predicate.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> notEq(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link RestrictionBuilder#eq(java.lang.String,java.lang.String, FullQueryBuilder) } but the result is wrapped in a NOT predicate.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as left hand side of a predicate
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> notEq(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link RestrictionBuilder#eq(java.lang.Object) } but the result is wrapped in a NOT predicate.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T notEq(Object value);

    /**
     * Like {@link RestrictionBuilder#eqExpression(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param expression The expression on the right hand side
     * @return The quantifiable binary predicate builder
     */
    public T notEqExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the GT predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> gt();

    /**
     * Starts a {@link SubqueryInitiator} for the GT predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> gt(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the GT predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the right hand side of the GT predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> gtSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the GT predicate that can be used to apply quantors.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> gt(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the GT predicate that can be used to apply quantors.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> gt(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the GT predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is greater than the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T gt(Object value);

    /**
     * Finishes the GT predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is greater than the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T gtExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the GE predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> ge();

    /**
     * Starts a {@link SubqueryInitiator} for the GE predicate that can be used to apply quantors.
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
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> ge(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the GE predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the right hand side of the GE predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> geSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the GE predicate that can be used to apply quantors.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> ge(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the GE predicate that can be used to apply quantors.
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
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> ge(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the GE predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is greater or equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T ge(Object value);

    /**
     * Finishes the GE predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is greater or equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T geExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the LT predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> lt();

    /**
     * Starts a {@link SubqueryInitiator} for the LT predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> lt(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the LT predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the right hand side of the LT predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> ltSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the LT predicate that can be used to apply quantors.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> lt(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the LT predicate that can be used to apply quantors.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> lt(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the LT predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is less than the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T lt(Object value);

    /**
     * Finishes the LT predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is less than the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T ltExpression(String expression);

    /**
     * Starts a {@link QuantifiableBinaryPredicateBuilder} for the LE predicate that can be used to apply quantors.
     *
     * @return The quantifiable binary predicate builder
     */
    public QuantifiableBinaryPredicateBuilder<T> le();

    /**
     * Starts a {@link SubqueryInitiator} for the LE predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The quantifiable subquery initiator
     */
    public SubqueryInitiator<T> le(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the LE predicate that can be used to apply quantors.
     * 
     * <p>
     * All occurrences of subsequently defined <code>subqueryAlias</code>es in <code>expression</code> will be replaced by the respective subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     * </p>
     * 
     * @param expression The expression for the right hand side of the LE predicate.
     * @return The subquery initiator for building multiple subqueries for their respective subqueryAliases
     * @since 1.2.0
     */
    public MultipleSubqueryInitiator<T> leSubqueries(String expression);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the LE predicate that can be used to apply quantors.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> le(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the LE predicate that can be used to apply quantors.
     *
     * <p>
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery. When the subquery
     * builder and the restriction builder for the right hand side are finished, the predicate is added to the parent predicate
     * container represented by the type <code>T</code>.
     * </p>
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression The expression which will be used as right hand side of a predicate.
     *            This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> le(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the LE predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is less or equal to the given value.
     *
     * @param value The value on the right hand side
     * @return The parent predicate container builder
     */
    public T le(Object value);

    /**
     * Finishes the LE predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is less or equal to the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T leExpression(String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of the IN predicate.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> in();

    /**
     * Like {@link RestrictionBuilder#in() } but the result is wrapped in a NOT predicate.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> notIn();

    /**
     * Starts a {@link SubqueryBuilder} based on the given criteria builder for the right hand side of the IN predicate.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type <code>T</code>.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> in(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Like {@link RestrictionBuilder#in(FullQueryBuilder) } but the result is wrapped in a NOT predicate.
     *
     * @param criteriaBuilder The criteria builder to base the subquery on
     * @return The subquery builder for building a subquery
     * @since 1.2.0
     */
    public SubqueryBuilder<T> notIn(FullQueryBuilder<?, ?> criteriaBuilder);

    /**
     * Finishes the IN predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is in the parameter- or literal-expressions denoted by the given parameters.
     *
     * @param parameterOrLiteralExpressions The parameter- or literal-expressions on the right hand side
     * @return The parent predicate container builder
     * @since 1.1.0
     */
    public T inExpressions(String... parameterOrLiteralExpressions);

    /**
     * Finishes the IN predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is in the list of given values.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T in(Collection<?> values);

    /**
     * Like {@link RestrictionBuilder#in(java.util.Collection) } but the values will be wrapped in a {@link Collection}.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T in(Object... values);

    /**
     * Like {@link RestrictionBuilder#inExpressions(String...) } but the result is wrapped in a NOT predicate.
     *
     * @param parameterOrLiteralExpressions The parameter- or literal-expressions on the right hand side
     * @return The parent predicate container builder
     * @since 1.1.0
     */
    public T notInExpressions(String... parameterOrLiteralExpressions);

    /**
     * Like {@link RestrictionBuilder#in(java.util.Collection) } but the result is wrapped in a NOT predicate.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T notIn(Collection<?> values);

    /**
     * Like {@link RestrictionBuilder#notIn(java.util.Collection) } but the values will be wrapped in a {@link Collection}.
     *
     * @param values The values on the right hand side
     * @return The parent predicate container builder
     */
    public T notIn(Object... values);

    /**
     * Finishes the IS NULL predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is null.
     *
     * @return The parent predicate container builder
     */
    public T isNull();

    /**
     * Like {@link RestrictionBuilder#isNull() } but the result is wrapped in a NOT predicate.
     *
     * @return The parent predicate container builder
     */
    public T isNotNull();

    /**
     * Finishes the IS EMPTY predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is empty.
     *
     * @return The parent predicate container builder
     */
    public T isEmpty();

    /**
     * Like {@link RestrictionBuilder#isEmpty() } but the result is wrapped in a NOT predicate.
     *
     * @return The parent predicate container builder
     */
    public T isNotEmpty();

    /**
     * Finishes the MEMBER OF predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     * The predicate checks if the left hand side is of the given expression.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T isMemberOf(String expression);

    /**
     * Like {@link RestrictionBuilder#isMemberOf(java.lang.String) } but the result is wrapped in a NOT predicate.
     *
     * @param expression The expression on the right hand side
     * @return The parent predicate container builder
     */
    public T isNotMemberOf(String expression);

    /**
     * Creates a {@link LikeBuilder} for building a like predicate.
     * 
     * <p>
     * The predicate checks if the left hand side is like the pattern delivered by the {@link LikeBuilder} while respecting the case
     * sensitivity according to the {@code caseSensitive} parameter.
     * </p>
     * 
     * <p>
     * Once the {@link LikeBuilder} is finished, the predicate is added to the parent predicate container represented by the type
     * <code>T</code>.
     * </p>
     * 
     * @param caseSensitive If true, the comparison of the left hand side expression with the pattern
     *            respects is performed case sensitive, else it is performed case insensitive.
     * @return The {@link LikeBuilder} for building the comparison pattern.
     */
    public LikeBuilder<T> like(boolean caseSensitive);

    /**
     * Like {@link RestrictionBuilder#like(boolean)} with {@code caseSensitive = true}.
     * 
     * @return The {@link LikeBuilder} for building the comparison pattern.
     */
    public LikeBuilder<T> like();

    /**
     * Like {@link RestrictionBuilder#like(boolean)} but the resulting like predicate is negated.
     * 
     * @param caseSensitive If true, the comparison of the left hand side expression with the pattern
     *            respects is performed case sensitive, else it is performed case insensitive.
     * @return The {@link LikeBuilder} for building the comparison pattern.
     */
    public LikeBuilder<T> notLike(boolean caseSensitive);

    /**
     * Like {@link RestrictionBuilder#notLike(boolean)} with {@code caseSensitive = true}.
     * 
     * @return The {@link LikeBuilder} for building the comparison pattern.
     */
    public LikeBuilder<T> notLike();

    /**
     * Switch to the non portable mode which might to be portable to other JPA providers.
     * 
     * @return this restriction builder in the non portable mode
     */
    public RestrictionBuilderExperimental<T> nonPortable();
}
