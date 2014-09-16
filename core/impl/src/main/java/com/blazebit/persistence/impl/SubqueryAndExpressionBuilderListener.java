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

import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;

/**
 *
 * @author Moritz Becker
 */
public class SubqueryAndExpressionBuilderListener<T> implements SubqueryBuilderListener<T>, ExpressionBuilderEndedListener {
    private final SubqueryBuilderListenerImpl<T> subqueryBuilderListener = new SubqueryBuilderListenerImpl<T>();
    private final ExpressionBuilderEndedListenerImpl expressionBuilderEndedListener = new ExpressionBuilderEndedListenerImpl();

    @Override
    public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
        subqueryBuilderListener.onBuilderEnded(builder);
    }

    @Override
    public void onBuilderStarted(SubqueryBuilderImpl<T> builder) {
        subqueryBuilderListener.onBuilderStarted(builder);
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        expressionBuilderEndedListener.onBuilderEnded(builder);
    }
    
    protected void verifyBuilderEnded() {
        expressionBuilderEndedListener.verifyBuilderEnded();
        subqueryBuilderListener.verifySubqueryBuilderEnded();
    }

    protected <T extends ExpressionBuilder> T startBuilder(T builder) {
        return expressionBuilderEndedListener.startBuilder(builder);
    }
}
