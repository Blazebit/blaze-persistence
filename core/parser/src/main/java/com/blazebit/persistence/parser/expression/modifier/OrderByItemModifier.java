/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.OrderByItem;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class OrderByItemModifier implements ExpressionModifier {

    protected final OrderByItem orderByItem;

    public OrderByItemModifier(OrderByItem orderByItem) {
        this.orderByItem = orderByItem;
    }

    @Override
    public void set(Expression expression) {
        orderByItem.setExpression(expression);
    }

    @Override
    public Expression get() {
        return orderByItem.getExpression();
    }

}
