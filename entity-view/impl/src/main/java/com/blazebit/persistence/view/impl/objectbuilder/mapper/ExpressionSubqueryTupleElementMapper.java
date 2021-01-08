/*
 * Copyright 2014 - 2021 Blazebit.
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

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ExpressionSubqueryTupleElementMapper implements SubqueryTupleElementMapper {

    protected final BasicUserTypeStringSupport<Object> basicTypeStringSupport;
    protected final SubqueryProvider provider;
    protected final String subqueryExpression;
    protected final String subqueryAlias;
    protected final String attributePath;
    protected final String viewPath;
    protected final String embeddingViewPath;

    public ExpressionSubqueryTupleElementMapper(Type<?> type, SubqueryProvider provider, String subqueryExpression, String subqueryAlias, String attributePath, String viewPath, String embeddingViewPath) {
        this.basicTypeStringSupport = TypeUtils.forType(type);
        this.provider = provider;
        this.subqueryExpression = subqueryExpression;
        this.subqueryAlias = subqueryAlias;
        this.attributePath = attributePath;
        this.viewPath = viewPath;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(null);
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        provider.createSubquery(subqueryInitiator(queryBuilder, viewJpqlMacro, embeddingViewJpqlMacro, asString));
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
    }

    protected SubqueryInitiator<?> subqueryInitiator(SelectBuilder<?> queryBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(viewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryInitiator<?> subqueryInitiator;
        if (asString && basicTypeStringSupport != null) {
            subqueryInitiator = queryBuilder.selectSubquery(subqueryAlias, basicTypeStringSupport.toStringExpression(subqueryExpression));
        } else {
            subqueryInitiator = queryBuilder.selectSubquery(subqueryAlias, subqueryExpression);
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
        return subqueryInitiator;
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public String getViewPath() {
        return viewPath;
    }

    @Override
    public String getEmbeddingViewPath() {
        return embeddingViewPath;
    }

    @Override
    public String getSubqueryAlias() {
        return subqueryAlias;
    }

    @Override
    public String getSubqueryExpression() {
        return subqueryExpression;
    }

    @Override
    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport() {
        return basicTypeStringSupport;
    }
}
