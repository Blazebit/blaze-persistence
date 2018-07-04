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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSubviewJoinTupleTransformerFactory implements TupleTransformerFactory {

    private final ViewTypeObjectBuilderTemplate<Object[]> template;
    private final CorrelationProviderFactory correlationProviderFactory;
    private final String correlationBasis;
    private final String correlationAlias;
    private final String correlationResult;
    private final String joinBase;
    private final String embeddingViewPath;
    private final String[] fetches;

    public CorrelatedSubviewJoinTupleTransformerFactory(ViewTypeObjectBuilderTemplate<Object[]> template, CorrelationProviderFactory correlationProviderFactory, String joinBase, String correlationBasis, String correlationResult, String attributePath, String embeddingViewPath, String[] fetches) {
        this.template = template;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationBasis = correlationBasis;
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        this.correlationResult = correlationResult;
        this.joinBase = joinBase;
        this.embeddingViewPath = embeddingViewPath;
        this.fetches = fetches;
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        // TODO: Fix view conversion for correlated attributes somehow
        // Before, we passed a FullQueryBuilder instead of a ParameterHolder but that doesn't work for view conversion
        // I'm not yet sure how view conversion could work with correlations, but casting the parameter holder here isn't very nice
        // For now it's ok, but at some point we will want to support correlated attributes somehow and need to think of a fallback solution here
        if (parameterHolder instanceof FullQueryBuilder<?, ?>) {
            FullQueryBuilder<?, ?> queryBuilder = (FullQueryBuilder<?, ?>) parameterHolder;
            CorrelationProvider provider = correlationProviderFactory.create(parameterHolder, optionalParameters);
            JoinCorrelationBuilder correlationBuilder = new JoinCorrelationBuilder(queryBuilder, optionalParameters, joinBase, correlationAlias, correlationResult, null);

            EmbeddingViewJpqlMacro embeddingViewJpqlMacro = entityViewConfiguration.getEmbeddingViewJpqlMacro();
            String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
            embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
            provider.applyCorrelation(correlationBuilder, correlationBasis);
            embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);

            if (fetches.length != 0) {
                for (int i = 0; i < fetches.length; i++) {
                    queryBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
                }
            }

            ObjectBuilder<Object[]> objectBuilder = template.createObjectBuilder(parameterHolder, optionalParameters, entityViewConfiguration, 0, true, false);
            return new CorrelatedSubviewJoinTupleTransformer(template, objectBuilder);
        } else {
            throw new UnsupportedOperationException("Converting views with correlated attributes isn't supported!");
        }
    }

}
