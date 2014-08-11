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
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class WhereAndBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements WhereAndBuilder<T>, PredicateBuilder {
    
    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final AndPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>>();
    private RightHandsideSubqueryPredicateBuilder rightSubqueryPredicateBuilderListener;
    
    public WhereAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
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
    public WhereOrBuilder<WhereAndBuilder<T>> whereOr() {
        return startBuilder(new WhereOrBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
    }
 
    @Override
    public RestrictionBuilder<WhereAndBuilder<T>> where(String expression) {
        Expression exp = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, exp, subqueryInitFactory, expressionFactory));
    }

    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }
    
    @Override
    public SubqueryInitiator<WhereAndBuilder<T>> whereNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder(this, new NotPredicate(new ExistsPredicate())));
        return subqueryInitFactory.createSubqueryInitiator((WhereAndBuilder<T>) this, rightSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery() {
        RestrictionBuilder<WhereAndBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator((RestrictionBuilder<WhereAndBuilder<T>>) restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereAndBuilder<T>>> whereSubquery(String subqueryAlias, String expression) {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
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
