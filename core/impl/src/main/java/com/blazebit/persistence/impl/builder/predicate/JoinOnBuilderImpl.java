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
import com.blazebit.persistence.JoinOnBuilder;
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

import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinOnBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements JoinOnBuilder<T>, PredicateBuilder {

    private final T result;
    private final RootPredicate rootPredicate;
    private final PredicateBuilderEndedListener listener;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<JoinOnBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
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
    public CaseWhenStarterBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onCase() {
        RestrictionBuilderImpl<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<JoinOnBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSimpleCase(String expression) {
        RestrictionBuilderImpl<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<JoinOnBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createCaseOperandExpression(expression)));
    }

    @Override
    public SubqueryInitiator<JoinOnBuilder<T>> onExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<JoinOnBuilder<T>> onNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnBuilder<T>> onExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnBuilder<T>> onNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery() {
        RestrictionBuilder<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<JoinOnBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.JOIN);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public JoinOnBuilder<T> onExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        rootPredicate.getPredicate().getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<JoinOnBuilder<T>> onExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<JoinOnBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<JoinOnBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                rootPredicate.getPredicate().getChildren().add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }

        }, subqueryInitFactory, ClauseType.JOIN);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
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
