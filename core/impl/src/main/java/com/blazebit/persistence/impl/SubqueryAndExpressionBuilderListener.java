/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryAndExpressionBuilderListener<T> implements SubqueryBuilderListener<T>, ExpressionBuilderEndedListener {

    private final SubqueryBuilderListenerImpl<T> subqueryBuilderListener = new SubqueryBuilderListenerImpl<T>();
    private final ExpressionBuilderEndedListenerImpl expressionBuilderEndedListener = new ExpressionBuilderEndedListenerImpl();

    @Override
    public void onReplaceBuilder(SubqueryInternalBuilder<T> oldBuilder, SubqueryInternalBuilder<T> newBuilder) {
        subqueryBuilderListener.onReplaceBuilder(oldBuilder, newBuilder);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        subqueryBuilderListener.onBuilderEnded(builder);
    }

    @Override
    public void onBuilderStarted(SubqueryInternalBuilder<T> builder) {
        subqueryBuilderListener.onBuilderStarted(builder);
    }

    @Override
    public void onInitiatorStarted(SubqueryInitiator<?> initiator) {
        subqueryBuilderListener.onInitiatorStarted(initiator);
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        expressionBuilderEndedListener.onBuilderEnded(builder);
    }

    public void startBuilder(MultipleSubqueryInitiatorImpl builder) {
        expressionBuilderEndedListener.startBuilder(builder);
    }

    protected void verifyBuilderEnded() {
        expressionBuilderEndedListener.verifyBuilderEnded();
        subqueryBuilderListener.verifySubqueryBuilderEnded();
    }

    protected <X extends ExpressionBuilder> X startBuilder(X builder) {
        return expressionBuilderEndedListener.startBuilder(builder);
    }
}
