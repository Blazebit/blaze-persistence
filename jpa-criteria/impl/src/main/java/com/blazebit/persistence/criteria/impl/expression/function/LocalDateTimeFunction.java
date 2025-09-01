/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import java.time.LocalDateTime;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LocalDateTimeFunction extends AbstractFunctionExpression<LocalDateTime> {

    public static final String NAME = "LOCAL_DATE_TIME";

    private static final long serialVersionUID = 1L;

    public LocalDateTimeFunction(BlazeCriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder, LocalDateTime.class, NAME);
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(getFunctionName());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X> BlazeExpression<X> as(Class<X> type) {
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return (BlazeExpression<X>) this;
        }
        return super.as(type);
    }
}
