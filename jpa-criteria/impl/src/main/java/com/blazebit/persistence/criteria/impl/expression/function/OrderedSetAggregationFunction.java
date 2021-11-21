/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeOrderedSetAggregateFunctionExpression;
import com.blazebit.persistence.criteria.BlazeWindow;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class OrderedSetAggregationFunction<T> extends AggregationFunctionExpressionImpl<T> implements BlazeOrderedSetAggregateFunctionExpression<T> {

    private static final long serialVersionUID = 1L;

    private List<BlazeOrder> withinGroupExpressions = Collections.emptyList();

    public OrderedSetAggregationFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType, String functionName, boolean distinct, Expression<?> argument) {
        super(criteriaBuilder, returnType, functionName, distinct, argument);
    }

    public OrderedSetAggregationFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, String functionName, boolean distinct, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, functionName, distinct, argumentExpressions);
    }

    @Override
    public List<BlazeOrder> getWithinGroup() {
        return withinGroupExpressions;
    }

    @Override
    public BlazeOrderedSetAggregateFunctionExpression<T> withinGroup(Order... orders) {
        this.withinGroupExpressions = (List<BlazeOrder>) (List<?>) Arrays.asList(orders);
        return this;
    }

    @Override
    public BlazeOrderedSetAggregateFunctionExpression<T> withinGroup(List<? extends Order> orders) {
        this.withinGroupExpressions = (List<BlazeOrder>) orders;
        return this;
    }
    @Override
    public BlazeOrderedSetAggregateFunctionExpression<T> filter(Predicate filter) {
        super.filter(filter);
        return this;
    }

    @Override
    public BlazeOrderedSetAggregateFunctionExpression<T> window(BlazeWindow window) {
        super.window(window);
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        super.visitParameters(visitor);
        if (withinGroupExpressions != null) {
            for (BlazeOrder withinGroupExpression : withinGroupExpressions) {
                ((AbstractSelection<?>) withinGroupExpression.getExpression()).visitParameters(visitor);
            }
        }
    }

    @Override
    protected void renderFilter(RenderContext context) {
        if (withinGroupExpressions != null && !withinGroupExpressions.isEmpty()) {
            StringBuilder buffer = context.getBuffer();
            buffer.append(" WITHIN GROUP (ORDER BY ");
            for (BlazeOrder withinGroupExpression : withinGroupExpressions) {
                ((AbstractSelection<?>) withinGroupExpression.getExpression()).render(context);
                if (withinGroupExpression.isAscending()) {
                    buffer.append(" ASC");
                } else {
                    buffer.append(" DESC");
                }
                if (withinGroupExpression.isNullsFirst()) {
                    buffer.append(" NULLS FIRST");
                } else {
                    buffer.append(" NULLS LAST");
                }
            }
            buffer.append(')');
        }
        super.renderFilter(context);
    }
}
