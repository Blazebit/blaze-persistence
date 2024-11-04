/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.metamodel.OrderByItem;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class Limiter {

    private final String limitParameter;
    private final Integer limitValue;
    private final String offsetParameter;
    private final Integer offsetValue;
    private final List<OrderByItem> orderByItems;

    public Limiter(String limitExpression, String offsetExpression, List<OrderByItem> orderByItems) {
        this.orderByItems = orderByItems;
        if (limitExpression.charAt(0) == ':') {
            this.limitParameter = limitExpression.substring(1);
            this.limitValue = null;
        } else {
            this.limitValue = Integer.parseInt(limitExpression);
            this.limitParameter = null;
        }
        if (offsetExpression.charAt(0) == ':') {
            this.offsetParameter = offsetExpression.substring(1);
            this.offsetValue = null;
        } else {
            this.offsetValue = Integer.parseInt(offsetExpression);
            this.offsetParameter = null;
        }
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public <T extends LimitBuilder<?> & OrderByBuilder<?>> void apply(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, T builder) {
        Integer limitValue = this.limitValue;
        if (limitValue == null) {
            limitValue = (Integer) optionalParameters.get(limitParameter);
            if (limitValue == null) {
                limitValue = (Integer) parameterHolder.getParameterValue(limitParameter);
            }
            if (limitValue == null) {
                return;
            }
        }
        for (OrderByItem orderByItem : orderByItems) {
            builder.orderBy(orderByItem.getExpression(), orderByItem.isAscending(), orderByItem.isNullsFirst());
        }
        builder.setMaxResults(limitValue);
        Integer offsetValue = this.offsetValue;
        if (offsetValue == null) {
            offsetValue = (Integer) optionalParameters.get(offsetParameter);
            if (offsetValue == null) {
                offsetValue = (Integer) parameterHolder.getParameterValue(offsetParameter);
            }
        }
        if (offsetValue != null) {
            builder.setFirstResult(offsetValue);
        }
    }
}
