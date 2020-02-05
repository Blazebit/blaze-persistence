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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.MultisetCorrelationBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CorrelationMultisetTupleElementMapper implements TupleElementMapper {

    protected final ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String correlationBasis;
    protected final String correlationAlias;
    protected final String attributePath;
    protected final String embeddingViewPath;

    public CorrelationMultisetTupleElementMapper(ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate, CorrelationProviderFactory correlationProviderFactory, String correlationBasis, String correlationAlias, String attributePath, String embeddingViewPath) {
        this.subviewTemplate = subviewTemplate;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationBasis = correlationBasis;
        this.correlationAlias = correlationAlias;
        this.attributePath = attributePath;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryInitiator<?> subqueryInitiator = queryBuilder.selectSubquery("subquery", "TO_MULTISET(subquery)");
        CorrelationProvider correlationProvider = correlationProviderFactory.create(parameterHolder, optionalParameters);
        MultisetCorrelationBuilder correlationBuilder = new MultisetCorrelationBuilder(subqueryInitiator, (ServiceProvider) queryBuilder, correlationAlias);
        correlationProvider.applyCorrelation(correlationBuilder, correlationBasis);
        SubqueryBuilder<?> subqueryBuilder = correlationBuilder.getSubqueryBuilder();
        for (TupleElementMapper mapper : subviewTemplate.getMappers()) {
            mapper.applyMapping(subqueryBuilder, parameterHolder, optionalParameters, embeddingViewJpqlMacro, true);
        }

        subqueryBuilder.end();
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport() {
        return null;
    }
}
