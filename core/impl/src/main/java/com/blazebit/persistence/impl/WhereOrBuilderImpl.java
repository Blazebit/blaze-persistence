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
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.predicate.BuilderEndedListener;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author cpbec
 */
public class WhereOrBuilderImpl<T> extends AbstractBuilderEndedListener implements WhereOrBuilder<T>, PredicateBuilder {

    private final T result;
    private final BuilderEndedListener listener;
    private final OrPredicate predicate;
    
    public WhereOrBuilderImpl(T result, BuilderEndedListener listener) {
        this.result = result;
        this.listener = listener;
        this.predicate = new OrPredicate();
    }
    
    @Override
    public T endOr() {
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
    public WhereAndBuilder<WhereOrBuilderImpl<T>> whereAnd() {
        return startBuilder(new WhereAndBuilderImpl<WhereOrBuilderImpl<T>>(this, this));
    }

    @Override
    public RestrictionBuilder<? extends WhereOrBuilder<T>> where(String expression) {
        return startBuilder(new RestrictionBuilderImpl<WhereOrBuilderImpl<T>>(this, this, Expressions.createSimpleExpression(expression)));
    }
    
}
