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

import java.util.List;

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseFinalSetOperationCTECriteriaBuilderImpl<T, X extends BaseFinalSetOperationBuilder<T, X>> extends BaseFinalSetOperationBuilderImpl<T, X, BaseFinalSetOperationCTECriteriaBuilderImpl<T, X>> implements BaseOngoingFinalSetOperationBuilder<T, X>, CTEInfoBuilder {

    protected final T result;
    protected final CTEBuilderListener listener;
    protected final FullSelectCTECriteriaBuilderImpl<?> initiator;
    protected final CTEBuilderListenerImpl subListener;
    
    public BaseFinalSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, T result, SetOperationType operator, boolean nested, CTEBuilderListener listener, FullSelectCTECriteriaBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, false, clazz, operator, nested, result);
        this.result = result;
        this.listener = listener;
        this.initiator = initiator;
        this.subListener = new CTEBuilderListenerImpl();
    }

    public BaseFinalSetOperationCTECriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, X, BaseFinalSetOperationCTECriteriaBuilderImpl<T, X>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.result = null;
        this.listener = null;
        this.initiator = null;
        this.subListener = null;
    }

    public FullSelectCTECriteriaBuilderImpl<?> getInitiator() {
        return initiator;
    }
    
    public T getResult() {
        return result;
    }

    public CTEBuilderListener getListener() {
        return listener;
    }

    public CTEBuilderListenerImpl getSubListener() {
        return subListener;
    }

    @Override
    public CTEInfo createCTEInfo() {
        return createCTEInfo(this, this);
    }
    
    private static CTEInfo createCTEInfo(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> target) {
        if (queryBuilder instanceof BaseFinalSetOperationCTECriteriaBuilderImpl<?, ?>) {
            BaseFinalSetOperationCTECriteriaBuilderImpl<?, ?> setOperationBuilder = (BaseFinalSetOperationCTECriteriaBuilderImpl<?, ?>) queryBuilder;
            
            if (setOperationBuilder.initiator == null) {
                return createCTEInfo(setOperationBuilder.setOperationManager.getStartQueryBuilder(), target);
            } else {
                List<String> attributes = setOperationBuilder.initiator.prepareAndGetAttributes();
                List<String> columns = setOperationBuilder.initiator.prepareAndGetColumnNames();
                CTEInfo info = new CTEInfo(setOperationBuilder.initiator.cteName, setOperationBuilder.initiator.cteType, attributes, columns, false, false, target, null);
                return info;
            }
        } else if (queryBuilder instanceof AbstractCTECriteriaBuilder<?, ?, ?, ?>) {
            AbstractCTECriteriaBuilder<?, ?, ?, ?> cteBuilder = (AbstractCTECriteriaBuilder<?, ?, ?, ?>) queryBuilder;
            List<String> attributes = cteBuilder.prepareAndGetAttributes();
            List<String> columns = cteBuilder.prepareAndGetColumnNames();
            CTEInfo info = new CTEInfo(cteBuilder.cteName, cteBuilder.cteType, attributes, columns, false, false, target, null);
            return info;
        }
        
        throw new IllegalArgumentException("Unsupported query builder type for creating a CTE info: " + queryBuilder);
    }

}
