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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PredicateAndExpressionBuilderEndedListener implements PredicateBuilderEndedListener, ExpressionBuilderEndedListener {

    private final PredicateBuilderEndedListenerImpl predicateBuilderListener = new PredicateBuilderEndedListenerImpl();
    private final ExpressionBuilderEndedListenerImpl expressionBuilderListener = new ExpressionBuilderEndedListenerImpl();

    @Override
    public void onBuilderEnded(PredicateBuilder o) {
        predicateBuilderListener.onBuilderEnded(o);
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        expressionBuilderListener.onBuilderEnded(builder);
    }

    protected void verifyBuilderEnded() {
        predicateBuilderListener.verifyBuilderEnded();
        expressionBuilderListener.verifyBuilderEnded();
    }

    protected <T extends PredicateBuilder> T startBuilder(T builder) {
        verifyBuilderEnded();
        return predicateBuilderListener.startBuilder(builder);
    }

    protected <T extends ExpressionBuilder> T startBuilder(T builder) {
        return expressionBuilderListener.startBuilder(builder);
    }
}
