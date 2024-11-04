/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryBuilderListenerImpl<T> implements SubqueryBuilderListener<T> {

    private SubqueryInitiator<?> currentSubqueryInitiator;
    private SubqueryInternalBuilder<T> currentSubqueryBuilder;

    public void onReplaceBuilder(SubqueryInternalBuilder<T> oldBuilder, SubqueryInternalBuilder<T> newBuilder) {
        if (currentSubqueryBuilder == null) {
            throw new BuilderChainingException("There was an attempt to replace a builder that was not started or already closed.");
        }
        if (currentSubqueryBuilder != oldBuilder) {
            throw new BuilderChainingException("There was an attempt to replace a builder that was not started or already closed.");
        }
        currentSubqueryBuilder = newBuilder;
    }

    public void verifySubqueryBuilderEnded() {
        if (currentSubqueryInitiator != null) {
            throw new BuilderChainingException("An initiator was not ended properly.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    protected SubqueryBuilder<T> startSubqueryBuilder(SubqueryBuilderImpl<T> subqueryBuilder) {
        verifySubqueryBuilderEnded();
        onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        if (currentSubqueryBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        currentSubqueryBuilder = null;
    }

    @Override
    public void onBuilderStarted(SubqueryInternalBuilder<T> builder) {
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }

        currentSubqueryInitiator = null;
        currentSubqueryBuilder = builder;
    }

    @Override
    public void onInitiatorStarted(SubqueryInitiator<?> initiator) {
        if (currentSubqueryInitiator != null) {
            throw new BuilderChainingException("There was an attempt to start an initiator but a previous initiator was not ended.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }

        currentSubqueryInitiator = initiator;
    }
}
