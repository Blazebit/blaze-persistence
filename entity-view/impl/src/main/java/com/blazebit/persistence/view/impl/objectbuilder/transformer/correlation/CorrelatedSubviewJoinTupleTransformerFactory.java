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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
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
    private final String[] fetches;

    public CorrelatedSubviewJoinTupleTransformerFactory(ViewTypeObjectBuilderTemplate<Object[]> template, CorrelationProviderFactory correlationProviderFactory, String correlationBasis, String correlationResult, String attributePath, String[] fetches) {
        this.template = template;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationBasis = correlationBasis;
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        this.correlationResult = correlationResult;
        this.fetches = fetches;
    }

    @Override
    public TupleTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        CorrelationProvider provider = correlationProviderFactory.create(queryBuilder, optionalParameters);
        JoinCorrelationBuilder correlationBuilder = new JoinCorrelationBuilder(queryBuilder, optionalParameters, correlationBasis, correlationAlias, correlationResult, null);
        provider.applyCorrelation(correlationBuilder, correlationBasis);

        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                queryBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }

        ObjectBuilder<Object[]> objectBuilder = template.createObjectBuilder(queryBuilder, optionalParameters, entityViewConfiguration, true, false);
        return new CorrelatedSubviewJoinTupleTransformer(template, objectBuilder);
    }

}
