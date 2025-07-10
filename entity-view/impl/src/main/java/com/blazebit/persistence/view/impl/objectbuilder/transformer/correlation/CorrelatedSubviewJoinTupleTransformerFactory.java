/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.StaticPathCorrelationProvider;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.NullTupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

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
    private final String correlationExternalAlias;
    private final String attributePath;
    private final String joinBase;
    private final String embeddingViewPath;
    private final String[] fetches;
    private final Limiter limiter;

    public CorrelatedSubviewJoinTupleTransformerFactory(ViewTypeObjectBuilderTemplate<Object[]> template, CorrelationProviderFactory correlationProviderFactory, String correlationAlias, String joinBase, String correlationBasis, String correlationExternalAlias, String attributePath, String embeddingViewPath, String[] fetches, Limiter limiter) {
        this.template = template;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationAlias = correlationAlias;
        this.correlationBasis = correlationBasis;
        this.correlationExternalAlias = correlationExternalAlias;
        this.attributePath = attributePath;
        this.joinBase = joinBase;
        this.embeddingViewPath = embeddingViewPath;
        this.fetches = fetches;
        this.limiter = limiter;
    }

    @Override
    public int getConsumeStartIndex() {
        return -1;
    }

    @Override
    public int getConsumeEndIndex() {
        return -1;
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        if (!entityViewConfiguration.hasSubFetches(attributePath)) {
            return new NullTupleTransformer(template, template.getTupleOffset());
        }
        // TODO: Fix view conversion for correlated attributes somehow
        // Before, we passed a FullQueryBuilder instead of a ParameterHolder but that doesn't work for view conversion
        // I'm not yet sure how view conversion could work with correlations, but casting the parameter holder here isn't very nice
        // For now it's ok, but at some point we will want to support correlated attributes somehow and need to think of a fallback solution here
        if (parameterHolder instanceof FullQueryBuilder<?, ?>) {
            FullQueryBuilder<?, ?> queryBuilder = (FullQueryBuilder<?, ?>) parameterHolder;
            CorrelationProvider provider = correlationProviderFactory.create(parameterHolder, optionalParameters);
            JoinCorrelationBuilder correlationBuilder = new JoinCorrelationBuilder(parameterHolder, optionalParameters, queryBuilder, joinBase, correlationAlias, correlationExternalAlias, attributePath, JoinType.LEFT, limiter);
            int originalFirstResult = -1;
            int originalMaxResults = -1;
            if (queryBuilder instanceof LimitBuilder<?>) {
                originalFirstResult = ((LimitBuilder<?>) queryBuilder).getFirstResult();
                originalMaxResults = ((LimitBuilder<?>) queryBuilder).getMaxResults();
            }

            ViewJpqlMacro viewJpqlMacro = entityViewConfiguration.getViewJpqlMacro();
            EmbeddingViewJpqlMacro embeddingViewJpqlMacro = entityViewConfiguration.getEmbeddingViewJpqlMacro();
            String oldViewPath = viewJpqlMacro.getViewPath();
            String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();

            viewJpqlMacro.setViewPath(joinBase);
            // If this uses a static path, we need to avoid setting the embedding view path etc.
            if (!(provider instanceof StaticPathCorrelationProvider)) {
                embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
            }

            provider.applyCorrelation(correlationBuilder, correlationBasis);

            if (queryBuilder instanceof LimitBuilder<?>) {
                if (originalFirstResult != ((LimitBuilder<?>) queryBuilder).getFirstResult()
                        || originalMaxResults != ((LimitBuilder<?>) queryBuilder).getMaxResults()) {
                    throw new IllegalArgumentException("Correlation provider '" + provider + "' wrongly uses setFirstResult() or setMaxResults() on the query builder which might lead to wrong results. Use SELECT fetching with batch size 1 or reformulate the correlation provider to use the limit/offset in a subquery!");
                }
            }

            correlationBuilder.finish();

            viewJpqlMacro.setViewPath(oldViewPath);
            if (!(provider instanceof StaticPathCorrelationProvider)) {
                embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
            }

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
