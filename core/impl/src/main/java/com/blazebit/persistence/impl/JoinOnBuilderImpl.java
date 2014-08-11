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

import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinOnBuilderImpl<X> implements JoinOnBuilder<X>, PredicateBuilder {

    private final X result;
    private final RootPredicate rootPredicate;
    private final PredicateBuilderEndedListener listener;
    private final ExpressionFactory expressionFactory;
    private final SubqueryInitiatorFactory subqueryInitFactory;

    public JoinOnBuilderImpl(X result, PredicateBuilderEndedListener listener, ParameterManager parameterManager, ExpressionFactory expressionFactory, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.rootPredicate = new RootPredicate(parameterManager);
        this.expressionFactory = expressionFactory;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public RestrictionBuilder<JoinOnBuilder<X>> on(String expression) {
        Expression leftExpression = expressionFactory.createSimpleExpression(expression);
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<JoinOnBuilder<X>>(this, rootPredicate, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public Predicate getPredicate() {
        return rootPredicate.predicate;
    }

    @Override
    public X end() {
        rootPredicate.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public JoinOnOrBuilder<JoinOnBuilder<X>> onOr() {
        return rootPredicate.startBuilder(new JoinOnOrBuilderImpl<JoinOnBuilder<X>>(this, rootPredicate, expressionFactory, subqueryInitFactory));
    }
}
