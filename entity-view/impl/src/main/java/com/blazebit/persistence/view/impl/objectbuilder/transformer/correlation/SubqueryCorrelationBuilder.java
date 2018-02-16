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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final String correlationAlias;
    private final String correlationResult;
    private final Class<?> correlationBasisType;
    private final Class<?> correlationBasisEntity;
    private final String correlationKeyAlias;
    private final int batchSize;
    private final boolean innerJoin;
    private String correlationRoot;

    public SubqueryCorrelationBuilder(FullQueryBuilder<?, ?> criteriaBuilder, String correlationAlias, String correlationResult, Class<?> correlationBasisType, Class<?> correlationBasisEntity, String correlationKeyAlias, int batchSize, boolean innerJoin, String attributePath) {
        this.criteriaBuilder = criteriaBuilder;
        this.correlationAlias = correlationAlias;
        this.correlationResult = correlationResult;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.correlationKeyAlias = correlationKeyAlias;
        this.batchSize = batchSize;
        this.innerJoin = innerJoin;
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
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        if (correlationRoot != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        JoinOnBuilder<CorrelationQueryBuilder> correlationBuilder;
        if (batchSize > 1) {
            if (correlationBasisEntity != null) {
                criteriaBuilder.fromIdentifiableValues(correlationBasisEntity, correlationKeyAlias, batchSize);
            } else {
                criteriaBuilder.fromValues(correlationBasisType, correlationKeyAlias, batchSize);
            }

            correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityClass, correlationAlias);
        } else {
            if (innerJoin) {
                correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityClass, correlationAlias);
            } else {
                criteriaBuilder.from(entityClass, correlationAlias);
                correlationBuilder = criteriaBuilder.getService(JoinOnBuilder.class);
            }
        }

        this.correlationRoot = correlationResult;
        return correlationBuilder;
    }

}
