/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.PredicateOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.PredicateAndBuilder;
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
public class PredicateOrBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements PredicateOrBuilder<T>, PredicateBuilder, WhereOrBuilder<T> {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate;
    private final ClauseType clauseType;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<PredicateOrBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<PredicateOrBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<PredicateOrBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public PredicateOrBuilderImpl(T result, PredicateBuilderEndedListener listener, ClauseType clauseType, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
        this.result = result;
        this.listener = listener;
        this.clauseType = clauseType;
        this.predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.OR);
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
    }

    @Override
    public T endOr() {
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
    public PredicateAndBuilder<PredicateOrBuilder<T>> and() {
        return startBuilder(new PredicateAndBuilderImpl<PredicateOrBuilder<T>>(this, this, clauseType, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public RestrictionBuilder<PredicateOrBuilder<T>> expression(String expression) {
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, exp, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<PredicateOrBuilder<T>>> selectCase() {
        RestrictionBuilderImpl<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<PredicateOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<PredicateOrBuilder<T>>> selectCase(String expression) {
        RestrictionBuilderImpl<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<PredicateOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, parameterManager, clauseType));
    }

    @Override
    public SubqueryInitiator<PredicateOrBuilder<T>> exists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((PredicateOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryInitiator<PredicateOrBuilder<T>> notExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((PredicateOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryBuilder<PredicateOrBuilder<T>> exists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((PredicateOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<PredicateOrBuilder<T>> notExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<PredicateOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((PredicateOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<PredicateOrBuilder<T>>> subquery() {
        RestrictionBuilder<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<PredicateOrBuilder<T>>> subquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<PredicateOrBuilder<T>>> subqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<PredicateOrBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<PredicateOrBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateOrBuilder<T>>> subquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<PredicateOrBuilder<T>>> subquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<PredicateOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<PredicateOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public PredicateOrBuilder<T> withExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        this.predicate.getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<PredicateOrBuilder<T>> withExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<PredicateOrBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<PredicateOrBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                PredicateOrBuilderImpl.this.predicate.getChildren().add((Predicate) builder.getExpression());
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
    public SubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery() {
        return (SubqueryInitiator) subquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator) subquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubqueries(String expression) {
        return (MultipleSubqueryInitiator) subqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) subquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public WhereOrBuilder<T> whereExpression(String expression) {
        withExpression(expression);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<WhereOrBuilder<T>> whereExpressionSubqueries(String expression) {
        return (MultipleSubqueryInitiator) withExpressionSubqueries(expression);
    }

    @Override
    public RestrictionBuilder<WhereOrBuilder<T>> where(String expression) {
        return (RestrictionBuilder) expression(expression);
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereCase() {
        return (CaseWhenStarterBuilder) selectCase();
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSimpleCase(String expression) {
        return (SimpleCaseWhenStarterBuilder) selectCase(expression);
    }

    @Override
    public SubqueryInitiator<WhereOrBuilder<T>> whereExists() {
        return (SubqueryInitiator) exists();
    }

    @Override
    public SubqueryInitiator<WhereOrBuilder<T>> whereNotExists() {
        return (SubqueryInitiator) notExists();
    }

    @Override
    public SubqueryBuilder<WhereOrBuilder<T>> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) exists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<WhereOrBuilder<T>> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return (SubqueryBuilder) notExists(criteriaBuilder);
    }

    @Override
    public WhereAndBuilder<WhereOrBuilder<T>> whereAnd() {
        return (WhereAndBuilder) and();
    }
}
