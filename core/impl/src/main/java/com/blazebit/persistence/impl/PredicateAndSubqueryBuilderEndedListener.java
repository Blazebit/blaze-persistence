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

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PredicateAndSubqueryBuilderEndedListener<T> implements PredicateBuilderEndedListener, SubqueryBuilderListener<T> {

    private final PredicateBuilderEndedListenerImpl predicateBuilderListener = new PredicateBuilderEndedListenerImpl();
    private final SubqueryBuilderListenerImpl<T> subqueryBuilderListener = new SubqueryBuilderListenerImpl<T>();

    @Override
    public void onBuilderEnded(PredicateBuilder o) {
        predicateBuilderListener.onBuilderEnded(o);
    }

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

    protected void verifyBuilderEnded() {
        predicateBuilderListener.verifyBuilderEnded();
        subqueryBuilderListener.verifySubqueryBuilderEnded();
    }

    protected <X extends PredicateBuilder> X startBuilder(X builder) {
        return predicateBuilderListener.startBuilder(builder);
    }
}
