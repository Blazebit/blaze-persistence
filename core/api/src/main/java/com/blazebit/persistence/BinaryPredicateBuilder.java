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
}
