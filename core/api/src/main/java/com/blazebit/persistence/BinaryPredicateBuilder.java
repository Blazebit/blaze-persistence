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
     * Finishes the binary predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     *
     * @param value The value to use for the right hand side of the binary predicate
     * @return The parent predicate container builder
     */
    public T value(Object value);

    /**
     * Uses the given expression as right hand side for the binary predicate.
     * Finishes the binary predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     *
     * @param expression The expression to use for the right hand side of the binary predicate
     * @return The parent predicate container builder
     */
    public T expression(String expression);
    
    
    /**
     * Starts a {@link RestrictionBuilder} to create a when predicate where expression will be on the left hand side of the predicate.
     *
     * @param expression The left hand expression for a when predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhen(String expression);

    /**
     * Starts a {@link SubqueryInitiator} for the left hand side of a when predicate.
     * When the subquery builder and the restriction builder for the right hand side are finished,
     * the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery();

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
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(String subqueryAlias, String expression);

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenExists();

    /**
     * Starts an exists predicate for the when clause with a subquery on the right hand side.
     * When the builder finishes, the when predicate in conjunction with it's then expression are added to the case when builder.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenNotExists();

    /**
     * Starts a {@link CaseWhenAndThenBuilder} which is a predicate consisting only of
     * conjunctive connected predicates. When the builder finishes, the when predicate
     * in conjunction with it's then expression are added to the case when builder.
     *
     * @return The and predicate builder for the when expression
     */
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> caseWhenAnd();

    /**
     * Starts a {@link CaseWhenOrThenBuilder} which is a predicate consisting only of
     * disjunctiv connected predicates. When the builder finishes, the when predicate
     * in conjunction with it's then expression are added to the case when builder.
     *
     * @return The or predicate builder for the when expression
     */
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> caseWhenOr();
}
