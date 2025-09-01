/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeOrder;
import jakarta.persistence.criteria.Nulls;

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

    public Nulls getNullPrecedence() {
        return nullsFirst ? Nulls.FIRST : Nulls.LAST;
    }

}
