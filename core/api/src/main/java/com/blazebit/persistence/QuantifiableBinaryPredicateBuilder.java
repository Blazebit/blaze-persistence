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
 * The interface for quantifiable binary predicate builders.
 * The left hand side and the operator are already known to the builder and the methods of this builder either terminate the building process or start a {@link SubqueryInitiator}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface QuantifiableBinaryPredicateBuilder<T> extends BinaryPredicateBuilder<T>, QuantifiableSubqueryInitiator<T> {
    
    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ALL quantor.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable binary predicate builder
     */
    public SubqueryInitiator<T> all(String subqueryAlias, String expression);
    
    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ANY quantor.
     * All occurrences of <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the subquery builder and the restriction builder for the right hand side are finished, the predicate is added to the
     * parent predicate container represented by the type {@linkplain T}.
     *
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be used as left hand side of a predicate
     * @return The quantifiable binary predicate builder
     */
    public SubqueryInitiator<T> any(String subqueryAlias, String expression);
}
