/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.CaseWhenAndBuilder;
import com.blazebit.persistence.CaseWhenOrBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class CaseWhenAndBuilderImpl<T> extends PredicateBuilderEndedListenerImpl implements CaseWhenAndBuilder<T>, PredicateBuilder {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private final CompoundPredicate predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
    private final PredicateBuilderEndedListener listener;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenAndBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();

    public CaseWhenAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.listener = listener;
        this.clauseType = clauseType;
    }

    @Override
    public RestrictionBuilder<CaseWhenAndBuilder<T>> and(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, expr, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery() {
        RestrictionBuilder<CaseWhenAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery(String subqueryAlias, String expression) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<CaseWhenAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubqueries(String expression) {
        return startMultipleSubqueryInitiator(expressionFactory.createSimpleExpression(expression));
    }

    private MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> startMultipleSubqueryInitiator(Expression expression) {
        verifyBuilderEnded();
        RestrictionBuilder<CaseWhenAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<CaseWhenAndBuilder<T>>>(restrictionBuilder, expression, null, subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public SubqueryInitiator<CaseWhenAndBuilder<T>> andExists() {
        SubqueryBuilderListenerImpl<CaseWhenAndBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryInitiator<CaseWhenAndBuilder<T>> andNotExists() {
        SubqueryBuilderListenerImpl<CaseWhenAndBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenAndBuilder<T>> andExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenAndBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenAndBuilder<T>> andNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenAndBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public CaseWhenOrBuilder<CaseWhenAndBuilder<T>> or() {
        return startBuilder(new CaseWhenOrBuilderImpl<CaseWhenAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public T endAnd() {
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public CompoundPredicate getPredicate() {
        return predicate;
    }

}
