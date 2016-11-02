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
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.view.CorrelationBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final String correlationResult;
    private String correlationRoot;

    public SubqueryCorrelationBuilder(CriteriaBuilder<?> criteriaBuilder, String correlationResult) {
        this.criteriaBuilder = criteriaBuilder;
        this.correlationResult = correlationResult;
    }

    public String getCorrelationRoot() {
        return correlationRoot;
    }

    @Override
    public JoinOnBuilder<BaseQueryBuilder<?, ?>> correlate(Class<?> entityClass, String alias) {
        criteriaBuilder.from(entityClass, alias);

        String correlationRoot;
        if (correlationResult.isEmpty()) {
            correlationRoot = alias;
        } else if (correlationResult.startsWith(alias) && (correlationResult.length() == alias.length() || correlationResult.charAt(alias.length()) == '.')) {
            correlationRoot = correlationResult;
        } else {
            correlationRoot = alias + '.' + correlationResult;
        }
        this.correlationRoot = correlationRoot;
        return criteriaBuilder.getService(JoinOnBuilder.class);
    }

}
