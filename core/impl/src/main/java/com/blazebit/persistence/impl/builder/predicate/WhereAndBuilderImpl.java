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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.RestrictionBuilderExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
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
public class WhereAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements WhereAndBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<WhereAndBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<WhereAndBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<WhereAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;

    public WhereAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
        this.result = result;
        this.listener = listener;
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
    public WhereOrBuilder<WhereAndBuilder<T>> whereOr() {
        return startBuilder(new WhereOrBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public RestrictionBuilder<WhereAndBuilder<T>> where(String expression) {
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, exp, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereCase() {
        RestrictionBuilderImpl<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<WhereAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSimpleCase(String expression) {
        RestrictionBuilderImpl<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<WhereAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createCaseOperandExpression(expression)));
    }

    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.WHERE);
    }

    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<WhereAndBuilder<T>> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<WhereAndBuilder<T>> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery() {
        RestrictionBuilder<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.WHERE);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.WHERE);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<WhereAndBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.WHERE);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
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
}
