package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeOrder;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

/**
 *
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
        ascending = !ascending;
        return this;
    }

    @Override
    public BlazeOrder reverseNulls() {
        nullsFirst = !nullsFirst;
        return this;
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
