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
 * The interface for binary predicate builders.
 * The left hand side and the operator are already known to the builder and the methods of this builder terminate the building process.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public interface BinaryPredicateBuilder<T> {

    /**
     * Uses the given value as right hand side for the binary predicate.
     * Finishes the binary predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @param value The value to use for the right hand side of the binary predicate
     * @return The parent predicate container builder
     */
    public T value(Object value);

    /**
     * Uses the given expression as right hand side for the binary predicate.
     * Finishes the binary predicate and adds it to the parent predicate container represented by the type <code>T</code>.
     *
     * @param expression The expression to use for the right hand side of the binary predicate
     * @return The parent predicate container builder
     */
    public T expression(String expression);
    
    /**
     * Starts a {@link RestrictionBuilder} to create a when expression with a single predicate
     * in which {@code expression} will be on the left hand side of the predicate.
     *
     * @see CaseWhenStarterBuilder#when(java.lang.String) More details about this method
     * 
     * @param expression The left hand side expression for a when predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhen(String expression);

    /**
     * Starts a {@link SubqueryInitiator} to create a when expression with a single predicate
     * in which the left hand side will be a subquery.
     * 
     * <p>
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenSubquery() More details about this method
     * 
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery();

    /**
     * Starts a {@link SubqueryInitiator} to create a when expression with a single predicate
     * in which the left hand side will be a subquery.
     * 
     * <p>
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenSubquery(java.lang.String, java.lang.String) More details about this method
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate.
     * This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(String subqueryAlias, String expression);

    /**
     * Starts a {@link SubqueryInitiator} to create a when expression with a single exists predicate.
     * 
     * <p>
     * When the builder finishes, the when predicate in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenExists() More details about this method
     * 
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenExists();

    /**
     * Starts a {@link SubqueryInitiator} to create a when expression with a single negated exists predicate.
     * 
     * <p>
     * When the builder finishes, the when predicate in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenNotExists() More details about this method
     * 
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenNotExists();

    /**
     * Starts a {@link CaseWhenAndThenBuilder} for building a when expression 
     * with conjunctively connected predicates.
     * 
     * <p>
     * When the builder finishes, the when predicate
     * in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenAnd() More details about this method
     * 
     * @return The and predicate builder for the when expression
     */
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> caseWhenAnd();

    /**
     * Starts a {@link CaseWhenOrThenBuilder} for building a when expression 
     * with disjunctively connected predicates.
     * 
     * 
     * <p>
     * When the builder finishes, the when predicate
     * in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see CaseWhenStarterBuilder#whenOr() More details about this method
     * 
     * @return The or predicate builder for the when expression
     */
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> caseWhenOr();
    
    /**
     * Starts a {@link SimpleCaseWhenBuilder} for building a simple case when expression.
     * 
     * <p>
     * When the builder finishes, the when predicate
     * in conjunction with its then expression are added to the case when builder.
     * </p>
     * 
     * @see SimpleCaseWhenStarterBuilder#when(java.lang.String, java.lang.String) More details about this method
     * 
     * @param caseOperand The case operand
     * @return The or predicate builder for the when expression
     */
    public SimpleCaseWhenBuilder<T> simpleCase(String caseOperand);
}
