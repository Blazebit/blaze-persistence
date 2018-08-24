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

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseFinalSetOperationCriteriaBuilderImpl<T, X extends BaseFinalSetOperationBuilder<T, X>> extends BaseFinalSetOperationBuilderImpl<T, X, BaseFinalSetOperationCriteriaBuilderImpl<T, X>> implements BaseOngoingFinalSetOperationBuilder<T, X> {

    private final BuilderListener<Object> listener;
    private final BuilderListenerImpl<Object> subListener;
    
    public BaseFinalSetOperationCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, BuilderListener<Object> listener, T endSetResult) {
        super(mainQuery, queryContext, isMainQuery, clazz, operator, nested, endSetResult);
        this.listener = listener;
        this.subListener = new BuilderListenerImpl<Object>();
    }

    public BaseFinalSetOperationCriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, X, BaseFinalSetOperationCriteriaBuilderImpl<T, X>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.listener = null;
        this.subListener = null;
    }

    public BuilderListener<Object> getListener() {
        return listener;
    }
    
    public BuilderListenerImpl<Object> getSubListener() {
        return subListener;
    }

    @Override
    protected void prepareAndCheck() {
        if (isMainQuery) {
            subListener.verifyBuilderEnded();
        }
        
        super.prepareAndCheck();
    }

}
