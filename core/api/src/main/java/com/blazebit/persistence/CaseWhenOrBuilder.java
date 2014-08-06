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
 * The builder interface for a when predicate container that connects predicates with the OR operator.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface CaseWhenOrBuilder<T> {

    /**
     * Starts a {@link RestrictionBuilder} for a case when predicate with the given expression as left hand expression.
     * When the builder finishes, the predicate is added this predicate container.
     *
     * @param expression The left hand expression for a case when predicate
     * @return The restriction builder for the given expression
     */
    public RestrictionBuilder<CaseWhenOrBuilder<T>> or(String expression);

    /**
     * Starts a case when and builder which connects it's predicates with the AND operator.
     * When the builder finishes, the predicate is added to this predicate container as disjunct.
     *
     * @return The case when and builder
     */
    public CaseWhenAndBuilder<CaseWhenOrBuilder<T>> and();

    /**
     * Finishes the OR predicate and adds it to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The parent predicate container builder
     */
    public T endOr();
}
