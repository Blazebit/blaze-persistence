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
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BuilderEndedListener;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author cpbec
 */
public class HavingAndBuilderImpl<T> extends AbstractBuilderEndedListener implements HavingAndBuilder<T>, PredicateBuilder {
    
    private final T result;
    private final BuilderEndedListener listener;
    private final AndPredicate predicate;
    
    public HavingAndBuilderImpl(T result, BuilderEndedListener listener) {
        this.result = result;
        this.listener = listener;
        this.predicate = new AndPredicate();
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
    public HavingOrBuilder<HavingAndBuilderImpl<T>> havingOr() {
        return startBuilder(new HavingOrBuilderImpl<HavingAndBuilderImpl<T>>(this, this));
    }

    @Override
    public RestrictionBuilder<? extends HavingAndBuilder<T>> having(String expression) {
        return startBuilder(new RestrictionBuilderImpl<HavingAndBuilderImpl<T>>(this, this, Expressions.createSimpleExpression(expression)));
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<? extends HavingAndBuilder<T>>> havingExists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
