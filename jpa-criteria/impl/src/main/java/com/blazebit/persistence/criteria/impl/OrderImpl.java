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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeOrder;

import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderImpl implements BlazeOrder {

    private final Expression<?> expression;
    private boolean ascending;
    private boolean nullsFirst;

    public OrderImpl(Expression<?> expression, boolean ascending, boolean nullsFirst) {
        this.expression = expression;
        this.ascending = ascending;
        this.nullsFirst = nullsFirst;
    }

    @Override
    public BlazeOrder reverse() {
        //        ascending = !ascending;
        //        return this;
        return new OrderImpl(expression, !ascending, nullsFirst);
    }

    @Override
    public BlazeOrder reverseNulls() {
        //        nullsFirst = !nullsFirst;
        //        return this;
        return new OrderImpl(expression, ascending, !nullsFirst);
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }

    @Override
    public boolean isNullsFirst() {
        return nullsFirst;
    }

    @Override
    public Expression<?> getExpression() {
        return expression;
    }

}
