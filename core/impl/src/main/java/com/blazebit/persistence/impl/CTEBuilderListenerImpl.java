/*
 * Copyright 2015 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class CTEBuilderListenerImpl implements CTEBuilderListener {

    private CTECriteriaBuilderImpl<?, ?> currentCteBuilder;

    public void verifySubqueryBuilderEnded() {
        if (currentCteBuilder != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    @Override
    public void onBuilderEnded(CTECriteriaBuilderImpl<?, ?> builder) {
        if (currentCteBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        currentCteBuilder = null;
    }

    @Override
    public void onBuilderStarted(CTECriteriaBuilderImpl<?, ?> builder) {
        if (currentCteBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }

        currentCteBuilder = builder;
    }
}
