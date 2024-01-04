/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.JoinOnOrBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinOnBuilderImpl<T> extends PredicateManager<JoinOnBuilderImpl<T>> implements JoinOnBuilder<T>, PredicateBuilder, PredicateBuilderEndedListener, SubqueryBuilderListener<T>, ExpressionBuilderEndedListener {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final PredicateBuilderEndedListenerImpl predicateBuilderListener = new PredicateBuilderEndedListenerImpl();
    private final SubqueryBuilderListenerImpl<T> subqueryBuilderListener = new SubqueryBuilderListenerImpl<T>();

    public JoinOnBuilderImpl(T result, final PredicateBuilderEndedListener listener, ParameterManager parameterManager, ExpressionFactory expressionFactory, SubqueryInitiatorFactory subqueryInitFactory) {
        super(subqueryInitFactory.getQueryBuilder().queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.result = result;
        this.listener = listener;
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.JOIN;
    }

    @Override
    protected String getClauseName() {
        return "ON";
    }

    @Override
    public RestrictionBuilder<JoinOnBuilder<T>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression, false);
        return restrict((JoinOnBuilder<T>) this, leftExpression);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onCase() {
        return restrictCase((JoinOnBuilder<T>) this);
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSimpleCase(String expression) {
        return restrictSimpleCase((JoinOnBuilder<T>) this, expressionFactory.createSimpleExpression(expression, false));
    }

    @Override
    public SubqueryInitiator<JoinOnBuilder<T>> onExists() {
        return restrictExists((JoinOnBuilder<T>) this);
    }

    @Override
    public SubqueryInitiator<JoinOnBuilder<T>> onNotExists() {
        return restrictNotExists((JoinOnBuilder<T>) this);
    }

    @Override
    public SubqueryBuilder<JoinOnBuilder<T>> onExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictExists((JoinOnBuilder<T>) this, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<JoinOnBuilder<T>> onNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictNotExists((JoinOnBuilder<T>) this, criteriaBuilder);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery() {
        return restrict((JoinOnBuilder<T>) this);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(String subqueryAlias, String expression) {
        return restrict((JoinOnBuilder<T>) this, subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<JoinOnBuilder<T>>> onSubqueries(String expression) {
        return restrictSubqueries((JoinOnBuilder<T>) this, expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict((JoinOnBuilder<T>) this, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnBuilder<T>>> onSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict((JoinOnBuilder<T>) this, subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public JoinOnBuilder<T> onExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictExpression(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<JoinOnBuilder<T>> onExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        return restrictExpressionSubqueries((JoinOnBuilder<T>) this, predicate);
    }

    @Override
    public T setOnExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictSetExpression(predicate);
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public MultipleSubqueryInitiator<T> setOnExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        return restrictSetExpressionSubqueries(result, predicate, this);
    }

    @Override
    public CompoundPredicate getPredicate() {
        return rootPredicate.getPredicate();
    }

    @Override
    public T end() {
        verifyEnded();
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        listener.onBuilderEnded(this);
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        predicateBuilderListener.onBuilderEnded(builder);
        rootPredicate.getPredicate().getChildren().add(builder.getPredicate());
    }

    @Override
    public JoinOnOrBuilder<JoinOnBuilder<T>> onOr() {
        return rootPredicate.startBuilder(new JoinOnOrBuilderImpl<JoinOnBuilder<T>>(this, rootPredicate, expressionFactory, parameterManager, subqueryInitFactory));
    }

    @Override
    public void onReplaceBuilder(SubqueryInternalBuilder<T> oldBuilder, SubqueryInternalBuilder<T> newBuilder) {
        subqueryBuilderListener.onReplaceBuilder(oldBuilder, newBuilder);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        subqueryBuilderListener.onBuilderEnded(builder);
    }

    @Override
    public void onBuilderStarted(SubqueryInternalBuilder<T> builder) {
        subqueryBuilderListener.onBuilderStarted(builder);
    }

    @Override
    public void onInitiatorStarted(SubqueryInitiator<?> initiator) {
        subqueryBuilderListener.onInitiatorStarted(initiator);
    }

    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        predicateBuilderListener.verifyBuilderEnded();
        subqueryBuilderListener.verifySubqueryBuilderEnded();
    }

    protected <X extends PredicateBuilder> X startBuilder(X builder) {
        return predicateBuilderListener.startBuilder(builder);
    }
}
