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

import java.util.List;

import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinOnBuilderImpl<T> implements JoinOnBuilder<T>, PredicateBuilder {

    private final T result;
    private final RootPredicate rootPredicate;
    private final PredicateBuilderEndedListener listener;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public JoinOnBuilderImpl(T result, final PredicateBuilderEndedListener listener, ParameterManager parameterManager, ExpressionFactory expressionFactory, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.rootPredicate = new RootPredicate(parameterManager, ClauseType.JOIN, subqueryInitFactory.getQueryBuilder());
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public RestrictionBuilder<JoinOnBuilder<T>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression, false);
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, rootPredicate, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public T setOnExpression(String expression) {
        rootPredicate.verifyBuilderEnded();
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        parameterManager.collectParameterRegistrations(predicate, ClauseType.JOIN, subqueryInitFactory.getQueryBuilder());
        
        List<Predicate> children = rootPredicate.getPredicate().getChildren();
        children.clear();
        children.add(predicate);
        listener.onBuilderEnded(JoinOnBuilderImpl.this);
        return result;
    }

    @Override
    public MultipleSubqueryInitiator<T> setOnExpressionSubqueries(String expression) {
        rootPredicate.verifyBuilderEnded();
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        parameterManager.collectParameterRegistrations(predicate, ClauseType.JOIN, subqueryInitFactory.getQueryBuilder());

        MultipleSubqueryInitiator<T> initiator = new MultipleSubqueryInitiatorImpl<T>(result, predicate, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                List<Predicate> children = rootPredicate.getPredicate().getChildren();
                children.clear();
                children.add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
                listener.onBuilderEnded(JoinOnBuilderImpl.this);
            }
            
        }, subqueryInitFactory, ClauseType.JOIN);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @Override
    public CompoundPredicate getPredicate() {
        return rootPredicate.getPredicate();
    }

    @Override
    public T end() {
        rootPredicate.verifyBuilderEnded();
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public JoinOnOrBuilder<JoinOnBuilder<T>> onOr() {
        return rootPredicate.startBuilder(new JoinOnOrBuilderImpl<JoinOnBuilder<T>>(this, rootPredicate, expressionFactory, parameterManager, subqueryInitFactory));
    }
}
