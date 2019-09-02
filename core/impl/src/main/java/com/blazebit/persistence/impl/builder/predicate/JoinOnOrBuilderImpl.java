/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnAndBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.RestrictionBuilderExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinOnOrBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements JoinOnOrBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.OR);
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnOrBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<JoinOnOrBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnOrBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public JoinOnOrBuilderImpl(T result, PredicateBuilderEndedListener listener, ExpressionFactory expressionFactory, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public T endOr() {
        verifyBuilderEnded();
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public JoinOnAndBuilder<JoinOnOrBuilder<T>> onAnd() {
        return startBuilder(new JoinOnAndBuilderImpl<JoinOnOrBuilder<T>>(this, this, expressionFactory, parameterManager, subqueryInitFactory));
    }

    @Override
    public RestrictionBuilder<JoinOnOrBuilder<T>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<JoinOnOrBuilder<T>>> onCase() {
        RestrictionBuilderImpl<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<JoinOnOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<JoinOnOrBuilder<T>>> onSimpleCase(String expression) {
        RestrictionBuilderImpl<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<JoinOnOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public SubqueryInitiator<JoinOnOrBuilder<T>> onExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<JoinOnOrBuilder<T>> onNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnOrBuilder<T>> onExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnOrBuilder<T>> onNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<JoinOnOrBuilder<T>>> onSubquery() {
        RestrictionBuilder<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<JoinOnOrBuilder<T>>> onSubquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<JoinOnOrBuilder<T>>> onSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<JoinOnOrBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<JoinOnOrBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.JOIN);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnOrBuilder<T>>> onSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnOrBuilder<T>>> onSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public JoinOnOrBuilder<T> onExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        this.predicate.getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<JoinOnOrBuilder<T>> onExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<JoinOnOrBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<JoinOnOrBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                JoinOnOrBuilderImpl.this.predicate.getChildren().add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }

        }, subqueryInitFactory, ClauseType.JOIN);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @Override
    public CompoundPredicate getPredicate() {
        return predicate;
    }

}
