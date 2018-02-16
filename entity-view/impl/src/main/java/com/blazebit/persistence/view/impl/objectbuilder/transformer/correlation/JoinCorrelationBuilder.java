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

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JoinCorrelationBuilder implements CorrelationBuilder {

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final Map<String, Object> optionalParameters;
    private final String joinBase;
    private final String selectAlias;
    private final String correlationAlias;
    private final String correlationResult;
    private boolean correlated;

    public JoinCorrelationBuilder(FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters, String joinBase, String correlationAlias, String correlationResult, String selectAlias) {
        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = optionalParameters;
        this.joinBase = joinBase;
        this.correlationAlias = correlationAlias;
        this.correlationResult = correlationResult;
        this.selectAlias = selectAlias;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return criteriaBuilder.getService(serviceClass);
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

        // Basic element has an alias, subviews don't
        if (selectAlias != null) {
            criteriaBuilder.select(correlationResult, selectAlias);
        }

        correlated = true;
        return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.leftJoinOn(joinBase, entityClass, correlationAlias);
    }

}
