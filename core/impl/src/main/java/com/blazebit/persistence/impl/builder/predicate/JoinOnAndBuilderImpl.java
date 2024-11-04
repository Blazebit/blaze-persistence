/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public class JoinOnAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements JoinOnAndBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnAndBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<JoinOnAndBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<JoinOnAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public JoinOnAndBuilderImpl(T result, PredicateBuilderEndedListener listener, ExpressionFactory expressionFactory, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public T endAnd() {
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
    public JoinOnOrBuilder<JoinOnAndBuilder<T>> onOr() {
        return startBuilder(new JoinOnOrBuilderImpl<JoinOnAndBuilder<T>>(this, this, expressionFactory, parameterManager, subqueryInitFactory));
    }

    @Override
    public RestrictionBuilder<JoinOnAndBuilder<T>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<JoinOnAndBuilder<T>>> onCase() {
        RestrictionBuilderImpl<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<JoinOnAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<JoinOnAndBuilder<T>>> onSimpleCase(String expression) {
        RestrictionBuilderImpl<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<JoinOnAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, parameterManager, ClauseType.JOIN));
    }

    @Override
    public SubqueryInitiator<JoinOnAndBuilder<T>> onExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<JoinOnAndBuilder<T>> onNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((JoinOnAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnAndBuilder<T>> onExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<JoinOnAndBuilder<T>> onNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<JoinOnAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((JoinOnAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<JoinOnAndBuilder<T>>> onSubquery() {
        RestrictionBuilder<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<JoinOnAndBuilder<T>>> onSubquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.JOIN);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<JoinOnAndBuilder<T>>> onSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<JoinOnAndBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<JoinOnAndBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.JOIN);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnAndBuilder<T>>> onSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<JoinOnAndBuilder<T>>> onSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<JoinOnAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<JoinOnAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.JOIN));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.JOIN);
    }

    @Override
    public JoinOnAndBuilder<T> onExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        this.predicate.getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<JoinOnAndBuilder<T>> onExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<JoinOnAndBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<JoinOnAndBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                JoinOnAndBuilderImpl.this.predicate.getChildren().add((Predicate) builder.getExpression());
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
