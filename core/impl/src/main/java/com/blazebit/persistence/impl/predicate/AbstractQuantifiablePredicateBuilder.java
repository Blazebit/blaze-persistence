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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.BinaryPredicateBuilder;
import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 */
public abstract class AbstractQuantifiablePredicateBuilder<T> implements
    QuantifiableBinaryPredicateBuilder<T>, PredicateBuilder {

    private final T result;
    private final BuilderEndedListener listener;
    private final boolean wrapNot;
    protected final Expression leftExpression;
    protected PredicateQuantifier quantifier = PredicateQuantifier.ONE;
    private Predicate predicate;

    public AbstractQuantifiablePredicateBuilder(T result, BuilderEndedListener listener, Expression leftExpression, boolean wrapNot) {
        this.result = result;
        this.listener = listener;
        this.wrapNot = wrapNot;
        this.leftExpression = leftExpression;
    }
    
    protected T chain(Predicate predicate) {
        this.predicate = wrapNot ? new NotPredicate(predicate) : predicate;
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public BinaryPredicateBuilder<T> all() {
        this.quantifier = PredicateQuantifier.ALL;
        return this;
    }

    @Override
    public BinaryPredicateBuilder<T> any() {
        this.quantifier = PredicateQuantifier.ANY;
        return this;
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }
}
