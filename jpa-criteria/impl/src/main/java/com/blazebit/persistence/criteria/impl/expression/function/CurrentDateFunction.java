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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import java.sql.Date;
import java.util.Calendar;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CurrentDateFunction extends AbstractFunctionExpression<Date> {

    public static final String NAME = "CURRENT_DATE";

    private static final long serialVersionUID = 1L;

    public CurrentDateFunction(BlazeCriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder, Date.class, NAME);
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
