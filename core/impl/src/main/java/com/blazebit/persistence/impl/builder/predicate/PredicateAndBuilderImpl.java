/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.PredicateAndBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.PredicateOrBuilder;
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
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
 * @author Christian Beikov
 * @since 1.6.8
 */
public class PredicateAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements PredicateAndBuilder<T>, PredicateBuilder, WhereAndBuilder<T> {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate;
    private final ClauseType clauseType;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<PredicateAndBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<PredicateAndBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<PredicateAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public PredicateAndBuilderImpl(T result, PredicateBuilderEndedListener listener, ClauseType clauseType, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
        this.result = result;
        this.listener = listener;
        this.clauseType = clauseType;
        this.predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
    }

    @Override
    public T endAnd() {
        verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public CompoundPredicate getPredicate() {
        return predicate;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public PredicateOrBuilder<PredicateAndBuilder<T>> or() {
        return startBuilder(new PredicateOrBuilderImpl<PredicateAndBuilder<T>>(this, this, clauseType, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public RestrictionBuilder<PredicateAndBuilder<T>> expression(String expression) {
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, exp, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<PredicateAndBuilder<T>>> selectCase() {
        RestrictionBuilderImpl<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<PredicateAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<PredicateAndBuilder<T>>> selectCase(String expression) {
        RestrictionBuilderImpl<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<PredicateAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, parameterManager, clauseType));
    }

    @Override
    public SubqueryInitiator<PredicateAndBuilder<T>> exists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((PredicateAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryInitiator<PredicateAndBuilder<T>> notExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((PredicateAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryBuilder<PredicateAndBuilder<T>> exists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((PredicateAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<PredicateAndBuilder<T>> notExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((PredicateAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<PredicateAndBuilder<T>>> subquery() {
        RestrictionBuilder<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<PredicateAndBuilder<T>>> subquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<PredicateAndBuilder<T>>> subqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<PredicateAndBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<PredicateAndBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateAndBuilder<T>>> subquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateAndBuilder<T>>> subquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<PredicateAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public PredicateAndBuilder<T> withExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        this.predicate.getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<PredicateAndBuilder<T>> withExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<PredicateAndBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<PredicateAndBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                PredicateAndBuilderImpl.this.predicate.getChildren().add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }

        }, subqueryInitFactory, clauseType);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if (rightSubqueryPredicateBuilderListener != null) {
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (superExprLeftSubqueryPredicateBuilderListener != null) {
            superExprLeftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (caseExpressionBuilderListener != null) {
            caseExpressionBuilderListener.verifyBuilderEnded();
        }
    }

    // todo: the following WhereOrBuilder implementation is just a temporary hack
    //  with https://github.com/Blazebit/blaze-persistence/issues/1596 we will remove this

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery() {
        return (SubqueryInitiator) subquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator) subquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubqueries(String expression) {
        return (MultipleSubqueryInitiator) subqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public WhereAndBuilder<T> whereExpression(String expression) {
        withExpression(expression);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<WhereAndBuilder<T>> whereExpressionSubqueries(String expression) {
        return (MultipleSubqueryInitiator) withExpressionSubqueries(expression);
    }

    @Override
    public RestrictionBuilder<WhereAndBuilder<T>> where(String expression) {
        return (RestrictionBuilder) expression(expression);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereCase() {
        return (CaseWhenStarterBuilder) selectCase();
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSimpleCase(String expression) {
        return (SimpleCaseWhenStarterBuilder) selectCase(expression);
    }

    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereExists() {
        return (SubqueryInitiator) exists();
    }

    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereNotExists() {
        return (SubqueryInitiator) notExists();
    }

    @Override
    public SubqueryBuilder<WhereAndBuilder<T>> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) exists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<WhereAndBuilder<T>> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) notExists(criteriaBuilder);
    }

    @Override
    public WhereOrBuilder<WhereAndBuilder<T>> whereOr() {
        return (WhereOrBuilder) or();
    }
}
