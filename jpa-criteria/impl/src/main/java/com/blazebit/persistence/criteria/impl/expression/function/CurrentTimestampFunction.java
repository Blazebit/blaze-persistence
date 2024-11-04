/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CurrentTimestampFunction extends AbstractFunctionExpression<Timestamp> {

    public static final String NAME = "CURRENT_TIMESTAMP";

    private static final long serialVersionUID = 1L;

    public CurrentTimestampFunction(BlazeCriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder, Timestamp.class, NAME);
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(getFunctionName());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X> BlazeExpression<X> as(Class<X> type) {
        if (java.util.Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
            return (BlazeExpression<X>) this;
        }
        return super.as(type);
    }
}
