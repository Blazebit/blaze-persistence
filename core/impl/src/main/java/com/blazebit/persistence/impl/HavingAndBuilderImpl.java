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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.HavingAndBuilder;
import com.blazebit.persistence.HavingOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.UnaryExpressionPredicate;

/**
 *
 * @author cpbec
 */
public class HavingAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements HavingAndBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final AndPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>>();
    private RightHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>> rightSubqueryPredicateBuilderListener;

    public HavingAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.listener = listener;
        this.predicate = new AndPredicate();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
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
        return startBuilder(new HavingOrBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
    }

    @Override
    public RestrictionBuilder<HavingAndBuilder<T>> having(String expression) {
        return startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, expressionFactory.createSimpleExpression(expression), subqueryInitFactory, expressionFactory));
    }

    @Override
    public SubqueryInitiator<HavingAndBuilder<T>> havingExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((HavingAndBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<HavingAndBuilder<T>> havingNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new NotPredicate(new ExistsPredicate())));
        return subqueryInitFactory.createSubqueryInitiator((HavingAndBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<HavingAndBuilder<T>>> having() {
        RestrictionBuilder<HavingAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<HavingAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator((RestrictionBuilder<HavingAndBuilder<T>>) restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }
    
    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if(rightSubqueryPredicateBuilderListener != null){
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
    }
}
