/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
