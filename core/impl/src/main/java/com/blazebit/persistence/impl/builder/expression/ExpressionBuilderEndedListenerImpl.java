/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.impl.BuilderChainingException;

/**
 * A base class that provides functionality to start and stop builders in a manner, such that only one builder can be started at a time.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ExpressionBuilderEndedListenerImpl implements ExpressionBuilderEndedListener {

    private ExpressionBuilder currentBuilder;

    public void verifyBuilderEnded() {
        if (currentBuilder != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    public <T extends ExpressionBuilder> T startBuilder(T builder) {
        if (currentBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }

        currentBuilder = builder;
        return builder;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        if (currentBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        currentBuilder = null;
    }
}
