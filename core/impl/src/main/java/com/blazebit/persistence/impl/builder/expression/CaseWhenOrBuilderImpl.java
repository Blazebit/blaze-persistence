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
public class CaseWhenOrBuilderImpl<T> extends PredicateBuilderEndedListenerImpl implements CaseWhenOrBuilder<T>, PredicateBuilder {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private final CompoundPredicate predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.OR);
    private final PredicateBuilderEndedListener listener;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenOrBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();

    public CaseWhenOrBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.listener = listener;
        this.clauseType = clauseType;
    }

    @Override
    public RestrictionBuilder<CaseWhenOrBuilder<T>> or(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, expr, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery() {
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery(String subqueryAlias, String expression) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenOrBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenOrBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubqueries(String expression) {
        return startMultipleSubqueryInitiator(expressionFactory.createArithmeticExpression(expression));
    }

    private MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> startMultipleSubqueryInitiator(Expression expression) {
        verifyBuilderEnded();
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<CaseWhenOrBuilder<T>>>(restrictionBuilder, expression, null, subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public SubqueryInitiator<CaseWhenOrBuilder<T>> orExists() {
        SubqueryBuilderListenerImpl<CaseWhenOrBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryInitiator<CaseWhenOrBuilder<T>> orNotExists() {
        SubqueryBuilderListenerImpl<CaseWhenOrBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenOrBuilder<T>> orExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenOrBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenOrBuilder<T>> orNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenOrBuilder<T>> rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, clauseType);
    }

    @Override
    public CaseWhenAndBuilder<CaseWhenOrBuilder<T>> and() {
        return startBuilder(new CaseWhenAndBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public T endOr() {
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public CompoundPredicate getPredicate() {
        return predicate;
    }

}
