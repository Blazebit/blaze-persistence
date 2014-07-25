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

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author cpbec
 */
public class WhereAndBuilderImpl<T> extends BuilderEndedListenerImpl implements WhereAndBuilder<T>, PredicateBuilder {
    
    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final AndPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    
    public WhereAndBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.predicate = new AndPredicate();
        this.subqueryInitFactory = subqueryInitFactory;
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
        return startBuilder(new WhereOrBuilderImpl<WhereAndBuilder<T>>(this, this, subqueryInitFactory));
    }
 
    @Override
    public RestrictionBuilder<? extends WhereAndBuilder<T>> where(String expression) {
        Expression exp = Expressions.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<WhereAndBuilderImpl<T>>(this, this, exp, subqueryInitFactory));
    }

    @Override
    public SubqueryInitiator<? extends WhereAndBuilder<T>> whereExists() {
        return subqueryInitFactory.createSubqueryInitiator(this, this);
    }
    
    @Override
    public SubqueryInitiator<? extends WhereAndBuilder<T>> whereNotExists() {
        return subqueryInitFactory.createSubqueryInitiator(this, this);
    }
}
