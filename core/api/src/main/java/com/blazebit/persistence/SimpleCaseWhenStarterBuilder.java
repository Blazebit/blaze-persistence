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
 * The first builder for simple case when expressions.
 *
 * This builder is used to enforce the correct usage of case when by disallowing an immediate call to
 * {@link SimpleCaseWhenBuilder#otherwise(java.lang.String)}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SimpleCaseWhenStarterBuilder<T> {

    /**
     * Adds the given when expression with the then expression to the case when builder.
     *
     * @param expression The when expression
     * @param thenExpression The then expression
     * @return This simple case when builder
     */
    public SimpleCaseWhenBuilder<T> when(String expression, String thenExpression);
    // TODO: subqueries?
}
