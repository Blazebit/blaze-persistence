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
    private final List<OrderByItem> orderByItems;

    public Limiter(String limitExpression, List<OrderByItem> orderByItems) {
        this.orderByItems = orderByItems;
        if (limitExpression.charAt(0) == ':') {
            this.limitParameter = limitExpression.substring(1);
            this.limitValue = null;
        } else {
            this.limitValue = Integer.parseInt(limitExpression);
            this.limitParameter = null;
        }
    }

    public <T extends LimitBuilder<?> & OrderByBuilder<?>> void apply(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, T builder) {
        if (limitValue == null) {
            Integer limitValue = (Integer) optionalParameters.get(limitParameter);
            if (limitValue == null) {
                limitValue = (Integer) parameterHolder.getParameterValue(limitParameter);
            }
            if (limitValue != null) {
                for (OrderByItem orderByItem : orderByItems) {
                    builder.orderBy(orderByItem.getExpression(), orderByItem.isAscending(), orderByItem.isNullsFirst());
                }
                builder.setMaxResults(limitValue);
            }
        } else {
            for (OrderByItem orderByItem : orderByItems) {
                builder.orderBy(orderByItem.getExpression(), orderByItem.isAscending(), orderByItem.isNullsFirst());
            }
            builder.setMaxResults(limitValue);
        }
    }
}
