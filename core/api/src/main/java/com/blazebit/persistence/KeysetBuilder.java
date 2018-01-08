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
 * A builder for keysets to make use of keyset pagination.
 * This is used for building a keyset declaratively.
 *
 * @param <T> The builder result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface KeysetBuilder<T> {

    /**
     * Uses the given value as reference value for keyset pagination for the given expression.
     * Normally the expression is one of the order by expressions used in the query.
     *
     * @param expression The order by expression for which a value should be provided
     * @param value The reference value from which the keyset pagination can start from
     * @return This keyset builder
     */
    public KeysetBuilder<T> with(String expression, Object value);

    /**
     * Finishes the keyset builder.
     *
     * @return The query builder
     */
    public T end();
}
