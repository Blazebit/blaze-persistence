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

import com.blazebit.persistence.FetchBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ExpressionTupleElementMapper implements TupleElementMapper {

    private static final String[] EMPTY = new String[0];

    protected final BasicUserTypeStringSupport<Object> basicTypeStringSupport;
    protected final String expression;
    protected final String attributePath;
    protected final String viewPath;
    protected final String embeddingViewPath;
    protected final String[] fetches;

    public ExpressionTupleElementMapper(BasicUserTypeStringSupport<Object> basicTypeStringSupport, String expression, String attributePath, String viewPath, String embeddingViewPath) {
        this.basicTypeStringSupport = basicTypeStringSupport;
        this.expression = expression;
        this.attributePath = attributePath;
        this.viewPath = viewPath == null ? null : viewPath.intern();
        this.embeddingViewPath = embeddingViewPath == null ? null : embeddingViewPath.intern();
        this.fetches = EMPTY;
    }

    public ExpressionTupleElementMapper(BasicUserTypeStringSupport<Object> basicTypeStringSupport, String expression, String attributePath, String viewPath, String embeddingViewPath, String[] fetches) {
        this.basicTypeStringSupport = basicTypeStringSupport;
        this.expression = expression;
        this.attributePath = attributePath;
        this.viewPath = viewPath == null ? null : viewPath.intern();
        this.embeddingViewPath = embeddingViewPath == null ? null : embeddingViewPath.intern();
        this.fetches = fetches;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(viewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        if (asString && basicTypeStringSupport != null) {
            queryBuilder.select(basicTypeStringSupport.toStringExpression(expression));
        } else {
            queryBuilder.select(expression);
        }
        if (fetches.length != 0) {
            final FetchBuilder<?> fetchBuilder = (FetchBuilder<?>) queryBuilder;
            for (int i = 0; i < fetches.length; i++) {
                fetchBuilder.fetch(fetches[i]);
            }
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport() {
        return basicTypeStringSupport;
    }

}
