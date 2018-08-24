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
import com.blazebit.persistence.HavingAndBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
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
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class HavingAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements HavingAndBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<HavingAndBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<HavingAndBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<HavingAndBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;

    public HavingAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
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
    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public HavingOrBuilder<HavingAndBuilder<T>> havingOr() {
        return startBuilder(new HavingOrBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public RestrictionBuilder<HavingAndBuilder<T>> having(String expression) {
        return startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<HavingAndBuilder<T>>> havingCase() {
        RestrictionBuilderImpl<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<HavingAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<HavingAndBuilder<T>>> havingSimpleCase(String expression) {
        RestrictionBuilderImpl<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<HavingAndBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createCaseOperandExpression(expression)));
    }

    @Override
    public SubqueryInitiator<HavingAndBuilder<T>> havingExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<HavingAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator(this, rightSubqueryPredicateBuilderListener, true, ClauseType.HAVING);
    }

    @Override
    public SubqueryInitiator<HavingAndBuilder<T>> havingNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<HavingAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator(this, rightSubqueryPredicateBuilderListener, true, ClauseType.HAVING);
    }

    @Override
    public SubqueryBuilder<HavingAndBuilder<T>> havingExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<HavingAndBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder(this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.HAVING);
    }

    @Override
    public SubqueryBuilder<HavingAndBuilder<T>> havingNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<HavingAndBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder(this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.HAVING);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<HavingAndBuilder<T>>> havingSubquery() {
        RestrictionBuilder<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.HAVING);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<HavingAndBuilder<T>>> havingSubquery(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.HAVING);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<HavingAndBuilder<T>>> havingSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<HavingAndBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<HavingAndBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.HAVING);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<HavingAndBuilder<T>>> havingSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.HAVING);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<HavingAndBuilder<T>>> havingSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expr);
        RestrictionBuilder<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.HAVING));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.HAVING);
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
