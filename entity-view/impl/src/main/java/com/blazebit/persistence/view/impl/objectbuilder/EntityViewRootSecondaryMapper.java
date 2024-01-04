/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.JoinCorrelationBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class EntityViewRootSecondaryMapper implements SecondaryMapper {

    private final String entityViewRootName;
    private final String attributePath;
    private final String viewPath;
    private final String embeddingViewPath;
    private final CorrelationProviderFactory correlationProviderFactory;
    private final JoinType joinType;
    private final String[] fetches;
    private final Limiter limiter;

    public EntityViewRootSecondaryMapper(String entityViewRootName, String attributePath, String viewPath, String embeddingViewPath, CorrelationProviderFactory correlationProviderFactory, JoinType joinType, String[] fetches, Limiter limiter) {
        this.entityViewRootName = entityViewRootName;
        this.attributePath = attributePath;
        this.viewPath = viewPath;
        this.embeddingViewPath = embeddingViewPath;
        this.correlationProviderFactory = correlationProviderFactory;
        this.joinType = joinType;
        this.fetches = fetches;
        this.limiter = limiter;
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public void apply(FullQueryBuilder<?, ?> fullQueryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(viewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        CorrelationProvider correlationProvider = correlationProviderFactory.create(parameterHolder, optionalParameters);
        String correlationAlias;
        if (limiter == null) {
            correlationAlias = entityViewRootName;
        } else {
            correlationAlias = "_sub_" + entityViewRootName;
        }
        JoinCorrelationBuilder correlationBuilder = new JoinCorrelationBuilder(parameterHolder, optionalParameters, fullQueryBuilder, viewJpqlMacro.getViewPath(), correlationAlias, entityViewRootName, attributePath, joinType, limiter);
        correlationProvider.applyCorrelation(correlationBuilder, viewJpqlMacro.getViewPath());
        correlationBuilder.finish();
        fullQueryBuilder.fetch(fetches);
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
    }
}
