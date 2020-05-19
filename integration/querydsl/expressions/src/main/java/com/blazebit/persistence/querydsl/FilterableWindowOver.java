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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Predicate;

/**
 * {@code FilterableWindowOver} is the first part of a WindowFunction construction.
 * Analog to {@link com.querydsl.sql.WindowOver}.
 *
 * @param <T> The window expression result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class FilterableWindowOver<T> extends WindowOver<T> {

    private static final long serialVersionUID = 6327480857012790191L;

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op) {
        super(type, op);
    }

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     * @param arg argument
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op, Expression<?> arg) {
        super(type, op, arg);
    }

    /**
     * Create a new window operation.
     *
     * @param type window expression result type
     * @param op operator
     * @param arg1 argument
     * @param arg2 argument
     */
    public FilterableWindowOver(Class<? extends T> type, Operator op, Expression<?> arg1, Expression<?> arg2) {
        super(type, op, arg1, arg2);
    }

    /**
     * Filter the values for this aggregate function.
     *
     * @param predicate Predicate to filter by
     * @return The filtered aggregate function
     */
    public WindowOver<T> filter(Predicate predicate) {
        return new WindowOver<T>(getType(), JPQLNextOps.FILTER, mixin, predicate);
    }

}
