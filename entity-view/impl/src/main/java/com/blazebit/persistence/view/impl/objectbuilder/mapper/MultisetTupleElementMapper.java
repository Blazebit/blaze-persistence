/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MultisetTupleElementMapper implements TupleElementMapper {

    private final ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate;
    private final String correlationExpression;
    private final String attributePath;
    private final String multisetResultAlias;
    private final String embeddingViewPath;
    private final String indexExpression;
    private final ViewTypeObjectBuilderTemplate<Object[]> indexTemplate;
    private final Limiter limiter;

    public MultisetTupleElementMapper(ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate, String correlationExpression, String attributePath, String multisetResultAlias, String embeddingViewPath, String indexExpression, ViewTypeObjectBuilderTemplate<Object[]> indexTemplate, Limiter limiter) {
        this.subviewTemplate = subviewTemplate;
        this.correlationExpression = correlationExpression.intern();
        this.attributePath = attributePath;
        this.multisetResultAlias = multisetResultAlias;
        this.embeddingViewPath = embeddingViewPath;
        this.indexExpression = indexExpression;
        this.indexTemplate = indexTemplate;
        this.limiter = limiter;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryBuilder<?> subqueryBuilder = queryBuilder.selectSubquery("subquery", "TO_MULTISET(subquery)")
                .from(correlationExpression, multisetResultAlias);
        for (TupleElementMapper mapper : subviewTemplate.getMappers()) {
            if (fetches == null || fetches.isEmpty() || ViewTypeObjectBuilder.hasSubFetches(fetches, attributePath + "." + mapper.getAttributePath())) {
                mapper.applyMapping(subqueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro,
                    embeddingViewJpqlMacro,
                    fetches, true);
            } else {
                subqueryBuilder.select("NULL");
            }
        }
        if (indexTemplate != null) {
            for (TupleElementMapper mapper : indexTemplate.getMappers()) {
                mapper.applyMapping(subqueryBuilder, parameterHolder, optionalParameters, viewJpqlMacro, embeddingViewJpqlMacro, null, true);
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
