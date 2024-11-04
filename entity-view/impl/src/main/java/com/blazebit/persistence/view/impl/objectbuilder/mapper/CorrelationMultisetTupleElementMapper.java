/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.MultisetCorrelationBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CorrelationMultisetTupleElementMapper implements TupleElementMapper {

    private final ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate;
    private final CorrelationProviderFactory correlationProviderFactory;
    private final String correlationBasis;
    private final String correlationAlias;
    private final String attributePath;
    private final String embeddingViewPath;
    private final String indexExpression;
    private final ViewTypeObjectBuilderTemplate<Object[]> indexTemplate;
    private final Limiter limiter;

    public CorrelationMultisetTupleElementMapper(ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate, CorrelationProviderFactory correlationProviderFactory, String correlationBasis, String correlationAlias, String attributePath, String embeddingViewPath, String indexExpression, ViewTypeObjectBuilderTemplate<Object[]> indexTemplate, Limiter limiter) {
        this.subviewTemplate = subviewTemplate;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationBasis = correlationBasis;
        this.correlationAlias = correlationAlias;
        this.attributePath = attributePath;
        this.embeddingViewPath = embeddingViewPath;
        this.indexExpression = indexExpression;
        this.indexTemplate = indexTemplate;
        this.limiter = limiter;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryInitiator<?> subqueryInitiator = queryBuilder.selectSubquery("subquery", "TO_MULTISET(subquery)");
        CorrelationProvider correlationProvider = correlationProviderFactory.create(parameterHolder, optionalParameters);
        MultisetCorrelationBuilder correlationBuilder = new MultisetCorrelationBuilder(subqueryInitiator, (ServiceProvider) queryBuilder, correlationAlias);
        correlationProvider.applyCorrelation(correlationBuilder, correlationBasis);
        SubqueryBuilder<?> subqueryBuilder = correlationBuilder.getSubqueryBuilder();
        for (TupleElementMapper mapper : subviewTemplate.getMappers()) {
            mapper.applyMapping(subqueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro, true);
        }
        if (indexTemplate != null) {
            for (TupleElementMapper mapper : indexTemplate.getMappers()) {
                mapper.applyMapping(subqueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro, true);
            }
        } else if (indexExpression != null) {
            subqueryBuilder.select(indexExpression);
        }

        if (limiter != null) {
            limiter.apply(parameterHolder, optionalParameters, subqueryBuilder);
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
