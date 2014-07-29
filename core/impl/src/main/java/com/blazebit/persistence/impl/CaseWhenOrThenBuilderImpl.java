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
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

/**
 *
 * @author cpbec
 */
public class CaseWhenOrThenBuilderImpl<T> extends BuilderEndedListenerImpl implements CaseWhenOrThenBuilder<T>, CaseWhenOrBuilder<T> {
    
    private final T result;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    
    public CaseWhenOrThenBuilderImpl(T result, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    @Override
    public RestrictionBuilder<CaseWhenOrThenBuilderImpl<T>> or(String expression) {
        return startBuilder(new RestrictionBuilderImpl<CaseWhenOrThenBuilderImpl<T>>(this, this, expressionFactory.createSimpleExpression(expression), subqueryInitFactory, expressionFactory));
    }
    
    @Override
    public CaseWhenAndBuilder<CaseWhenOrThenBuilderImpl<T>> and() {
        return new CaseWhenAndThenBuilderImpl<CaseWhenOrThenBuilderImpl<T>>(this, subqueryInitFactory, expressionFactory);
    }

    @Override
    public T then(String expression) {
        return result;
    }

    @Override
    public T endOr() {
        return result;
    }
    
}
