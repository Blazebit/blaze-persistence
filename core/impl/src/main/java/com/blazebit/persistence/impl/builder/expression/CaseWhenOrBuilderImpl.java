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

import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.CaseWhenAndBuilder;
import com.blazebit.persistence.CaseWhenOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.predicate.LeftHandsideSubqueryPredicateBuilderListener;
import com.blazebit.persistence.impl.builder.predicate.RightHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.SuperExpressionLeftHandsideSubqueryPredicateBuilder;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CaseWhenOrBuilderImpl<T> extends PredicateBuilderEndedListenerImpl implements CaseWhenOrBuilder<T>, PredicateBuilder {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final OrPredicate predicate = new OrPredicate();
    private final PredicateBuilderEndedListener listener;
    private final LeftHandsideSubqueryPredicateBuilderListener leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();

    public CaseWhenOrBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.listener = listener;
    }

    @Override
    public RestrictionBuilder<CaseWhenOrBuilder<T>> or(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, expr, subqueryInitFactory, expressionFactory, false));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery() {
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(
            new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, true));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrBuilder<T>>> orSubquery(String subqueryAlias, String expression) {
        SuperExpressionLeftHandsideSubqueryPredicateBuilder superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression));
        RestrictionBuilder<CaseWhenOrBuilder<T>> restrictionBuilder = startBuilder(
            new RestrictionBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, true));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder,
                                                           superExprLeftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<CaseWhenOrBuilder<T>> orExists() {
        RightHandsideSubqueryPredicateBuilder rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<CaseWhenOrBuilder<T>> orNotExists() {
        RightHandsideSubqueryPredicateBuilder rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((CaseWhenOrBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public CaseWhenAndBuilder<CaseWhenOrBuilder<T>> and() {
        return startBuilder(new CaseWhenAndBuilderImpl<CaseWhenOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
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
    public Predicate getPredicate() {
        return predicate;
    }

}
