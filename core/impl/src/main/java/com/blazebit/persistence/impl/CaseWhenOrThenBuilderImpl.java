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
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

/**
 * TODO: implement
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CaseWhenOrThenBuilderImpl<T extends CaseWhenBuilder<?>> extends PredicateBuilderEndedListenerImpl implements
    CaseWhenOrThenBuilder<T> {

    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    public CaseWhenOrThenBuilderImpl(T result, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    @Override
    public RestrictionBuilder<CaseWhenOrThenBuilder<T>> or(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        return startBuilder(new RestrictionBuilderImpl<CaseWhenOrThenBuilder<T>>(this, this, expr, subqueryInitFactory, expressionFactory, false));
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenOrThenBuilder<T>>> orSubquery(String subqueryAlias, String expression) {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenOrThenBuilder<T>> orExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SubqueryInitiator<CaseWhenOrThenBuilder<T>> orNotExists() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CaseWhenAndBuilder<CaseWhenOrThenBuilder<T>> and() {
        return new CaseWhenAndBuilderImpl<CaseWhenOrThenBuilder<T>>(this, subqueryInitFactory, expressionFactory);
    }

    @Override
    public T then(String expression) {
        return result;
    }

}
