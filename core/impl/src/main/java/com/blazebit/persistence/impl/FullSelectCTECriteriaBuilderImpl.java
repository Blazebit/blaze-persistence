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

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FullSelectCTECriteriaBuilderImpl<T> extends AbstractCTECriteriaBuilder<T, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>>, FinalSetOperationCTECriteriaBuilderImpl<Object>> implements FullSelectCTECriteriaBuilder<T> {

	public FullSelectCTECriteriaBuilderImpl(MainQuery mainQuery, Class<Object> clazz, T result, CTEBuilderListener listener) {
		super(mainQuery, clazz, null, result, listener);
	}
    
    @Override
    protected FinalSetOperationCTECriteriaBuilderImpl<Object> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return new FinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, resultType, result, operator, nested, listener, this);
    }

    @Override
    protected LeafOngoingSetOperationCTECriteriaBuilder<T> createSetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder) {
        return new LeafOngoingSetOperationCTECriteriaBuilderImpl<T>(mainQuery, (Class<Object>) resultType, finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>> createSubquerySetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, FinalSetOperationCTECriteriaBuilderImpl<Object> resultFinalSetOperationBuilder) {
        LeafOngoingSetOperationCTECriteriaBuilderImpl<T> leafCb = new LeafOngoingSetOperationCTECriteriaBuilderImpl<T>(mainQuery, (Class<Object>) resultType, resultFinalSetOperationBuilder);
        return new OngoingSetOperationCTECriteriaBuilderImpl<T, LeafOngoingSetOperationCTECriteriaBuilder<T>>(mainQuery, (Class<Object>) resultType, finalSetOperationBuilder, leafCb);
    }

}
