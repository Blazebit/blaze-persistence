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

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
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
public class CaseWhenAndThenBuilderImpl<T extends CaseWhenBuilder<?>> extends PredicateBuilderEndedListenerImpl implements
    CaseWhenAndThenBuilder<T> {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public CaseWhenAndThenBuilderImpl(T result, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    @Override
    public RestrictionBuilder<CaseWhenAndThenBuilder<T>> and(String expression) {
        return startBuilder(new RestrictionBuilderImpl<CaseWhenAndThenBuilder<T>>(this, this, expressionFactory.createSimpleExpression(expression), subqueryInitFactory,
                                                                                  expressionFactory, false));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> andSubquery() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenAndThenBuilder<T>>> andSubquery(String subqueryAlias, String expression) {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenAndThenBuilder<T>> andExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenAndThenBuilder<T>> andNotExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CaseWhenOrBuilder<CaseWhenAndThenBuilder<T>> or() {
        return new CaseWhenOrBuilderImpl<CaseWhenAndThenBuilder<T>>(this, subqueryInitFactory, expressionFactory);
    }

    @Override
    public T then(String expression) {
        return result;
    }

}
