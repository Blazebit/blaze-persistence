/*
 * Copyright 2014 - 2019 Blazebit.
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
 * A builder that can terminate the build process for general case when expressions.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CaseWhenThenBuilder<T extends CaseWhenBuilder<?>> {

    /**
     * Adds the constructed when expression with the then expression to the case when builder.
     *
     * @param expression The then expression
     * @return This case when builder
     */
    public T thenExpression(String expression);

    /**
     * Adds the constructed when expression with the then parameter value to the case when builder rendered as literal.
     *
     * @param value The then parameter value
     * @return This case when builder
     * @since 1.4.0
     */
    public T thenLiteral(Object value);

    /**
     * Adds the constructed when expression with the then parameter value to the case when builder.
     *
     * @param value The then parameter value
     * @return This case when builder
     */
    public T then(Object value);
    
    // TODO: add subqueries variants?
}
