/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.CompoundPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CaseWhenAndThenBuilderImpl<T extends CaseWhenBuilder<?>> extends PredicateBuilderEndedListenerImpl implements CaseWhenAndThenBuilder<T>, ExpressionBuilder {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final CompoundPredicate predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
    private final ExpressionBuilderEndedListener listener;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private WhenClauseExpression whenClause;

    public CaseWhenAndThenBuilderImpl(T result, ExpressionBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.listener = listener;
    }

    @Override
    public RestrictionBuilder<CaseWhenAndThenBuilder<T>> and(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, expr, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> andSubquery() {
        RestrictionBuilder<CaseWhenAndThenBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> andSubquery(String subqueryAlias, String expression) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression));
        RestrictionBuilder<CaseWhenAndThenBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> andSubqueries(String expression) {
        return startMultipleSubqueryInitiator(expressionFactory.createArithmeticExpression(expression));
    }

    private MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> startMultipleSubqueryInitiator(Expression expression) {
        verifyBuilderEnded();
        RestrictionBuilder<CaseWhenAndThenBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<CaseWhenAndThenBuilder<T>>>(restrictionBuilder, expression, null, subqueryInitFactory);
        return initiator;
    }

    @Override
    public SubqueryInitiator<CaseWhenAndThenBuilder<T>> andExists() {
        SubqueryBuilderListenerImpl<CaseWhenAndThenBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndThenBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenAndThenBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<CaseWhenAndThenBuilder<T>> andNotExists() {
        SubqueryBuilderListenerImpl<CaseWhenAndThenBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenAndThenBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenAndThenBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public CaseWhenOrBuilder<CaseWhenAndThenBuilder<T>> or() {
        return startBuilder(new CaseWhenOrBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public T thenExpression(String expression) {
        verifyBuilderEnded();
        if (predicate.getChildren().isEmpty()) {
            throw new IllegalStateException("No and clauses specified!");
        }
        if (whenClause != null) {
            throw new IllegalStateException("Method then/thenExpression called multiple times");
        }
        whenClause = new WhenClauseExpression(predicate, expressionFactory.createScalarExpression(expression));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public T then(Object value) {
        verifyBuilderEnded();
        if (predicate.getChildren().isEmpty()) {
            throw new IllegalStateException("No and clauses specified!");
        }
        if (whenClause != null) {
            throw new IllegalStateException("Method then/thenExpression called multiple times");
        }
        whenClause = new WhenClauseExpression(predicate, parameterManager.addParameterExpression(value));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public Expression getExpression() {
        return whenClause;
    }

}
