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

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.PredicateAndExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class CaseWhenBuilderImpl<T> extends PredicateAndExpressionBuilderEndedListener implements CaseWhenBuilder<T>, CaseWhenThenBuilder<CaseWhenBuilder<T>>, ExpressionBuilder {

    private final T result;
    private final List<WhenClauseExpression> whenClauses;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;

    private Predicate whenExpression;
    private GeneralCaseExpression expression;
    private Expression thenExpression;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private final ExpressionBuilderEndedListener listener;

    public CaseWhenBuilderImpl(T result, ExpressionBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        this.result = result;
        this.whenClauses = new ArrayList<WhenClauseExpression>();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.listener = listener;
        this.clauseType = clauseType;
    }

    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> when(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, expr, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery() {
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubqueryBuilderListenerImpl<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, clauseType);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> whenSubqueries(String expression) {
        return startMultipleSubqueryInitiator(expressionFactory.createArithmeticExpression(expression));
    }

    private MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> startMultipleSubqueryInitiator(Expression expression) {
        verifyBuilderEnded();
        RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>>(restrictionBuilder, expression, null, subqueryInitFactory, clauseType);
        return initiator;
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenExists() {
        SubqueryBuilderListenerImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>> rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside, true, clauseType);
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenNotExists() {
        SubqueryBuilderListenerImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>> rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside, true, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>> rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside, true, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> whenNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        SubqueryBuilderListenerImpl<CaseWhenThenBuilder<CaseWhenBuilder<T>>> rightHandside = startBuilder(new RightHandsideSubqueryPredicateBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((CaseWhenThenBuilder<CaseWhenBuilder<T>>) this, rightHandside, true, criteriaBuilder, clauseType);
    }

    @Override
    public CaseWhenBuilder<T> thenExpression(String expression) {
        // verify that thenExpression is called for the first time
        if (thenExpression != null) {
            throw new IllegalStateException("Method then/thenExpression called multiple times");
        }
        thenExpression = expressionFactory.createScalarExpression(expression);
        whenClauses.add(new WhenClauseExpression(whenExpression, thenExpression));
        return this;
    }

    @Override
    public CaseWhenBuilder<T> then(Object value) {
        if (thenExpression != null) {
            throw new IllegalStateException("Method then/thenExpression called multiple times");
        }
        thenExpression = parameterManager.addParameterExpression(value, clauseType, subqueryInitFactory.getQueryBuilder());
        whenClauses.add(new WhenClauseExpression(whenExpression, thenExpression));
        return this;
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> whenAnd() {
        return startBuilder(new CaseWhenAndThenBuilderImpl<CaseWhenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> whenOr() {
        return startBuilder(new CaseWhenOrThenBuilderImpl<CaseWhenBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType));
    }

    @Override
    public T otherwiseExpression(String elseExpression) {
        verifyBuilderEnded();
        if (whenClauses.isEmpty()) {
            throw new IllegalStateException("No when clauses specified");
        }
        if (expression != null) {
            throw new IllegalStateException("Method otherwise/otherwiseExpression called multiple times");
        }
        expression = new GeneralCaseExpression(whenClauses, expressionFactory.createScalarExpression(elseExpression));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public T otherwise(Object value) {
        verifyBuilderEnded();
        if (whenClauses.isEmpty()) {
            throw new IllegalStateException("No when clauses specified");
        }
        if (expression != null) {
            throw new IllegalStateException("Method otherwise/otherwiseExpression called multiple times");
        }
        expression = new GeneralCaseExpression(whenClauses, parameterManager.addParameterExpression(value, clauseType, subqueryInitFactory.getQueryBuilder()));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder o) {
        super.onBuilderEnded(o);
        this.whenExpression = o.getPredicate();
        this.thenExpression = null;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        super.onBuilderEnded(builder);
        whenClauses.add((WhenClauseExpression) builder.getExpression());
    }

    @Override
    public void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

}
