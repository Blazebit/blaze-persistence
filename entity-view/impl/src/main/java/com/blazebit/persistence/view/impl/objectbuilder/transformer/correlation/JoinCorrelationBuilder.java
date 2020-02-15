/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;

import javax.persistence.metamodel.EntityType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JoinCorrelationBuilder implements CorrelationBuilder {

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final String joinBase;
    private final String correlationAlias;
    private boolean correlated;

    public JoinCorrelationBuilder(FullQueryBuilder<?, ?> criteriaBuilder, String joinBase, String correlationAlias) {
        this.criteriaBuilder = criteriaBuilder;
        this.joinBase = joinBase;
        this.correlationAlias = correlationAlias;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return criteriaBuilder.getService(serviceClass);
    }

    @Override
    public FromProvider getCorrelationFromProvider() {
        return criteriaBuilder;
    }

    @Override
    public String getCorrelationAlias() {
        return correlationAlias;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        if (correlated) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        correlated = true;
        return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.leftJoinOn(joinBase, entityClass, correlationAlias);
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
        if (correlated) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        correlated = true;
        return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.leftJoinOn(joinBase, entityType, correlationAlias);
    }
}
