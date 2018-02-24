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
