/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.Fetchable;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.SubQueryExpression;

import javax.annotation.Nonnegative;

/**
 * Set expresion defines an interface for set operation queries.
 * Analog to {@link com.querydsl.sql.Union}, but also used for {@code INTERSECT} and {@code EXCEPT} operations.
 *
 * @param <RT> return type of the projection
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public interface SetExpression<RT> extends SubQueryExpression<RT>, Fetchable<RT>, ExtendedFetchable<RT> {

    /**
     * Set the limit / max results for the query results
     *
     * @param limit max rows
     * @return the current object
     */
    SetExpression<RT> limit(@Nonnegative long limit);

    /**
     * Set the offset for the query results
     *
     * @param offset row offset
     * @return the current object
     */
    SetExpression<RT> offset(@Nonnegative long offset);

    /**
     * Define the ordering of the query results
     *
     * @param o order
     * @return the current object
     */
    SetExpression<RT> orderBy(OrderSpecifier<?>... o);

}
