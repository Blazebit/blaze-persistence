/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final String correlationResult;
    private final Class<?> correlationBasisType;
    private final Class<?> correlationBasisEntity;
    private final String correllationKeyAlias;
    private final int batchSize;
    private final boolean innerJoin;
    private final String correlationAlias;
    private String correlationRoot;

    public SubqueryCorrelationBuilder(FullQueryBuilder<?, ?> criteriaBuilder, String correlationResult, Class<?> correlationBasisType, Class<?> correlationBasisEntity, String correllationKeyAlias, int batchSize, boolean innerJoin, String attributePath) {
        this.criteriaBuilder = criteriaBuilder;
        this.correlationResult = correlationResult;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.correllationKeyAlias = correllationKeyAlias;
        this.batchSize = batchSize;
        this.innerJoin = innerJoin;
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return criteriaBuilder.getService(serviceClass);
    }

    @Override
    public String getCorrelationAlias() {
        return correlationAlias;
    }

    public String getCorrelationRoot() {
        return correlationRoot;
    }

    @Override
    public JoinOnBuilder<ParameterHolder<?>> correlate(Class<?> entityClass) {
        if (correlationRoot != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        JoinOnBuilder<ParameterHolder<?>> correlationBuilder;
        if (batchSize > 1) {
            if (correlationBasisEntity != null) {
                criteriaBuilder.fromIdentifiableValues(correlationBasisEntity, correllationKeyAlias, batchSize);
            } else {
                criteriaBuilder.fromValues(correlationBasisType, correllationKeyAlias, batchSize);
            }

            correlationBuilder = (JoinOnBuilder<ParameterHolder<?>>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityClass, correlationAlias);
        } else {
            if (innerJoin) {
                correlationBuilder = (JoinOnBuilder<ParameterHolder<?>>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityClass, correlationAlias);
            } else {
                criteriaBuilder.from(entityClass, correlationAlias);
                correlationBuilder = criteriaBuilder.getService(JoinOnBuilder.class);
            }
        }

        String correlationRoot;
        if (correlationResult.isEmpty()) {
            correlationRoot = correlationAlias;
        } else {
            correlationRoot = correlationAlias + '.' + correlationResult;
        }
        this.correlationRoot = correlationRoot;
        return correlationBuilder;
    }

}
