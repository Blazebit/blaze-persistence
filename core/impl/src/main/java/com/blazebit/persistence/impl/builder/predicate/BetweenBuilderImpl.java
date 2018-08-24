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

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class BetweenBuilderImpl<T> extends SubqueryBuilderListenerImpl<T> implements BetweenBuilder<T>, LeftHandsideSubqueryPredicateBuilder {

    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private final PredicateBuilderEndedListener listener;
    private final Expression left;
    private final boolean negated;
    private Expression start;
    private final T result;
    private BetweenPredicate predicate;
    private SubqueryInitiator<?> subqueryStartMarker;

    public BetweenBuilderImpl(T result, Expression left, Expression start, ExpressionFactory expressionFactory, ParameterManager parameterManager, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ClauseType clauseType) {
        this(result, left, start, expressionFactory, parameterManager, listener, subqueryInitFactory, clauseType, false);
    }

    public BetweenBuilderImpl(T result, Expression left, Expression start, ExpressionFactory expressionFactory, ParameterManager parameterManager, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ClauseType clauseType, boolean negated) {
        this.result = result;
        this.left = left;
        this.start = start;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.clauseType = clauseType;
        this.listener = listener;
        this.subqueryInitFactory = subqueryInitFactory;
        this.negated = negated;
    }

    @Override
    public T and(Object end) {
        if (end == null) {
            throw new NullPointerException("end");
        }
        return chain(new BetweenPredicate(left, start, parameterManager.addParameterExpression(end, clauseType, subqueryInitFactory.getQueryBuilder()), negated));
    }

    @Override
    public T andExpression(String end) {
        return chain(new BetweenPredicate(left, start, expressionFactory.createArithmeticExpression(end), negated));
    }

    @Override
    public SubqueryInitiator<T> andSubqery() {
        verifySubqueryBuilderEnded();
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator(result, this, false, clauseType));
    }

    @Override
    public SubqueryInitiator<T> andSubqery(String subqueryAlias, String expression) {
        verifySubqueryBuilderEnded();
        SubqueryBuilderListenerImpl<T> superExpressionSubqueryListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, expressionFactory.createArithmeticExpression(expression)) {

            @Override
            public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
                super.onBuilderEnded(builder);
                predicate = new BetweenPredicate(left, start, superExpression, negated);
                listener.onBuilderEnded(BetweenBuilderImpl.this);
            }

        };
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator(result, superExpressionSubqueryListener, false, clauseType));
    }

    @Override
    public SubqueryBuilder<T> andSubqery(FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        return startSubqueryBuilder(subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clauseType));
    }

    @Override
    public SubqueryBuilder<T> andSubqery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        SubqueryBuilderListenerImpl<T> superExpressionSubqueryListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, expressionFactory.createArithmeticExpression(expression)) {

            @Override
            public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
                super.onBuilderEnded(builder);
                predicate = new BetweenPredicate(left, start, superExpression, negated);
                listener.onBuilderEnded(BetweenBuilderImpl.this);
            }

        };
        return startSubqueryBuilder(subqueryInitFactory.createSubqueryBuilder(result, superExpressionSubqueryListener, false, criteriaBuilder, clauseType));
    }

    @Override
    public MultipleSubqueryInitiator<T> andSubqueries(String expression) {
        return startMultipleSubqueryInitiator(expressionFactory.createArithmeticExpression(expression));
    }

    private MultipleSubqueryInitiator<T> startMultipleSubqueryInitiator(Expression expression) {
        verifySubqueryBuilderEnded();
        MultipleSubqueryInitiator<T> initiator = new MultipleSubqueryInitiatorImpl<T>(result, expression, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                predicate = new BetweenPredicate(left, start, builder.getExpression(), negated);
                listener.onBuilderEnded(BetweenBuilderImpl.this);
            }
            
        }, subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public BetweenPredicate getPredicate() {
        return predicate;
    }

    @Override
    public void setLeftExpression(Expression start) {
        this.start = start;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        this.subqueryStartMarker = null;
        this.predicate = new BetweenPredicate(left, start, new SubqueryExpression(builder), negated);
        listener.onBuilderEnded(this);
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

    private T chain(BetweenPredicate predicate) {
        verifySubqueryBuilderEnded();
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }
}
