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
import com.blazebit.persistence.impl.expression.Expressions;

/**
 *
 * @author cpbec
 */
public class CaseWhenOrThenBuilderImpl<T> extends AbstractBuilderEndedListener implements CaseWhenOrThenBuilder<T>, CaseWhenOrBuilder<T> {
    
    private final T result;

    public CaseWhenOrThenBuilderImpl(T result) {
        this.result = result;
    }

    @Override
    public RestrictionBuilder<CaseWhenOrThenBuilderImpl<T>> or(String expression) {
        return startBuilder(new RestrictionBuilderImpl<CaseWhenOrThenBuilderImpl<T>>(this, this, Expressions.createSimpleExpression(expression)));
    }
    
    @Override
    public CaseWhenAndBuilder<CaseWhenOrThenBuilderImpl<T>> and() {
        return new CaseWhenAndThenBuilderImpl<CaseWhenOrThenBuilderImpl<T>>(this);
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
