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

import java.util.List;

import javax.persistence.Tuple;

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FinalSetOperationSubqueryBuilderImpl<T> extends BaseFinalSetOperationBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>, FinalSetOperationSubqueryBuilderImpl<T>> implements FinalSetOperationSubqueryBuilder<T>, SubqueryInternalBuilder<T> {

    private final T result;
    private final SubqueryBuilderListener<T> listener;
    private final SubqueryBuilderImpl<?> initiator;
    
    @SuppressWarnings("unchecked")
    public FinalSetOperationSubqueryBuilderImpl(MainQuery mainQuery, T result, SetOperationType operator, boolean nested, SubqueryBuilderListener<T> listener, SubqueryBuilderImpl<?> initiator) {
        super(mainQuery, false, (Class<T>) Tuple.class, operator, nested);
        this.result = result;
        this.listener = listener;
        this.initiator = initiator;
    }

    public SubqueryBuilderListener<T> getListener() {
        return listener;
    }
    
    public SubqueryBuilderImpl<?> getInitiator() {
        return initiator;
    }


    @Override
    public T end() {
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public List<Expression> getSelectExpressions() {
        return initiator.getSelectExpressions();
    }

}
