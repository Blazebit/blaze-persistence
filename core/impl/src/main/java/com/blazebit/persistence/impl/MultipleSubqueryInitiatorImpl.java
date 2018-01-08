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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

public class MultipleSubqueryInitiatorImpl<T> extends SubqueryBuilderListenerImpl<MultipleSubqueryInitiator<T>> implements MultipleSubqueryInitiator<T>, ExpressionBuilder {

    private final T result;
    private final ExpressionBuilderEndedListener listener;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private Expression expression;
    private String subqueryAlias;
    private SubqueryInitiator<?> subqueryStartMarker;
    
    public MultipleSubqueryInitiatorImpl(T result, Expression expression, ExpressionBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.expression = expression;
        this.listener = listener;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SubqueryInitiator<MultipleSubqueryInitiator<T>> with(String subqueryAlias) {
        verifySubqueryBuilderEnded();
        this.subqueryAlias = subqueryAlias;
        // The cast with the type parameter sucks but I don't want to spend too much time with that right now 
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator(this, this, false));
    }

    @Override
    public SubqueryBuilder<MultipleSubqueryInitiator<T>> with(String subqueryAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        this.subqueryAlias = subqueryAlias;
        // The cast with the type parameter sucks but I don't want to spend too much time with that right now
        return startSubqueryBuilder(subqueryInitFactory.createSubqueryBuilder(this, this, false, criteriaBuilder));
    }

    @Override
    public T end() {
        if (listener != null) {
            listener.onBuilderEnded(this);
        }
        
        return result;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<MultipleSubqueryInitiator<T>> builder) {
        super.onBuilderEnded(builder);
        expression = ExpressionUtils.replaceSubexpression(expression, subqueryAlias, new SubqueryExpression(builder));
        subqueryStartMarker = null;
        subqueryAlias = null;
    }

    @Override
    public void verifySubqueryBuilderEnded() {
        if (subqueryStartMarker != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        super.verifySubqueryBuilderEnded();
    }

    public <X> SubqueryInitiator<X> startSubqueryInitiator(SubqueryInitiator<X> subqueryInitiator) {
        this.subqueryStartMarker = subqueryInitiator;
        return subqueryInitiator;
    }

}
