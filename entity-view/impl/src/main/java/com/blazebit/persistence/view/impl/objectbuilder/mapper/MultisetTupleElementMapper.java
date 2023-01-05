/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;

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
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryBuilder<?> subqueryBuilder = queryBuilder.selectSubquery("subquery", "TO_MULTISET(subquery)")
                .from(correlationExpression, multisetResultAlias);
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
