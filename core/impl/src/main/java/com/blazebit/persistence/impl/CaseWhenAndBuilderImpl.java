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

import com.blazebit.persistence.CaseWhenAndBuilder;
import com.blazebit.persistence.CaseWhenOrBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

/**
 * TODO: implement
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CaseWhenAndBuilderImpl<T> extends PredicateBuilderEndedListenerImpl implements CaseWhenAndBuilder<T> {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public CaseWhenAndBuilderImpl(T result, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    @Override
    public RestrictionBuilder<CaseWhenAndBuilder<T>> and(String expression) {
        return startBuilder(new RestrictionBuilderImpl<CaseWhenAndBuilder<T>>(this, this, expressionFactory.createSimpleExpression(expression), subqueryInitFactory,
                                                                              expressionFactory));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndBuilder<T>>> andSubquery(String subqueryAlias, String expression) {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenAndBuilder<T>> andExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenAndBuilder<T>> andNotExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CaseWhenOrBuilder<CaseWhenAndBuilder<T>> or() {
        return new CaseWhenOrBuilderImpl<CaseWhenAndBuilder<T>>(this, subqueryInitFactory, expressionFactory);
    }

    @Override
    public T endAnd() {
        return result;
    }

}
