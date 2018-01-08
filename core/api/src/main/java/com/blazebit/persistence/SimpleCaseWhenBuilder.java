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
 * A builder for simple case when expressions.
 *
 * The left hand expression also referred to as case operand, will be compared to the when expressions defined via
 * {@link SimpleCaseWhenBuilder#when(java.lang.String, java.lang.String)}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SimpleCaseWhenBuilder<T> extends SimpleCaseWhenStarterBuilder<T> {

    /**
     * Adds the given else expression to the case when builder.
     *
     * @param elseExpression The else expression
     * @return The parent builder
     */
    public T otherwise(String elseExpression);
    // TODO: subqueries?
}
