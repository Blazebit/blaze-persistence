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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Expression;

/**
 * An extended version of {@link Expression}.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeExpression<X> extends Expression<X> {

    /* Covariant overrides */

    /**
     * Like {@link Expression#as} but returns the subtype {@link BlazeExpression} instead.
     *
     * @param type intended type of the expression
     * @param <X>  The intended expression type
     * @return A new expression of the given type
     */
    <X> BlazeExpression<X> as(Class<X> type);
}
