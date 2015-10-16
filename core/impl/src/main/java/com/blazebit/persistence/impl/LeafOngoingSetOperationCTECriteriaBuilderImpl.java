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

import com.blazebit.persistence.FinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationCTECriteriaBuilderImpl<T> extends AbstractCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>>, FinalSetOperationCTECriteriaBuilderImpl<Object>> implements LeafOngoingSetOperationCTECriteriaBuilder<T> {

    public LeafOngoingSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, Class<Object> clazz, T result, CTEBuilderListener listener, FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder) {
        super(mainQuery, clazz, result, listener, finalSetOperationBuilder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FinalSetOperationCTECriteriaBuilder<T> endSet() {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return (FinalSetOperationCTECriteriaBuilder<T>) finalSetOperationBuilder;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected FinalSetOperationCTECriteriaBuilderImpl<Object> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        // TODO: not quite sure about the listener passing
        FinalSetOperationCTECriteriaBuilderImpl<T> currentSetOperationBuilder = (FinalSetOperationCTECriteriaBuilderImpl<T>) finalSetOperationBuilder;
        return new FinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, resultType, result, operator, nested, currentSetOperationBuilder.getListener(), currentSetOperationBuilder.getInitiator());
    }

    @Override
    protected LeafOngoingSetOperationCTECriteriaBuilder<T> createSetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>> createSubquerySetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, FinalSetOperationCTECriteriaBuilderImpl<Object> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        LeafOngoingSetOperationCTECriteriaBuilder<T> leafCb = createLeaf(resultFinalSetOperationBuilder);
        return createOngoing(finalSetOperationBuilder, leafCb);
    }

}
