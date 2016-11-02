/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.BaseQueryBuilder;
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
    private final String correlationBasis;
    private final String correlationResult;
    private final String selectAlias;

    public JoinCorrelationBuilder(FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters, String correlationBasis, String correlationResult, String selectAlias) {
        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = optionalParameters;
        this.correlationBasis = correlationBasis;
        this.correlationResult = correlationResult;
        this.selectAlias = selectAlias;
    }

    @Override
    public JoinOnBuilder<BaseQueryBuilder<?, ?>> correlate(Class<?> entityClass, String alias) {
        String selectExpression;
        if (correlationResult.isEmpty()) {
            selectExpression = alias;
        } else if (correlationResult.startsWith(alias) && (correlationResult.length() == alias.length() || correlationResult.charAt(alias.length()) == '.')) {
            selectExpression = correlationResult;
        } else {
            selectExpression = alias + '.' + correlationResult;
        }

        // Basic element has an alias, subviews don't
        if (selectAlias != null) {
            criteriaBuilder.select(selectExpression, selectAlias);
        }

        return (JoinOnBuilder<BaseQueryBuilder<?, ?>>) (JoinOnBuilder<?>) criteriaBuilder.leftJoinOn(correlationBasis, entityClass, alias);
    }

}
