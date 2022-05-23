/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterizedAliasExpressionSubqueryTupleElementMapper extends ParameterizedExpressionSubqueryTupleElementMapper implements AliasedTupleElementMapper {

    private final String alias;

    public ParameterizedAliasExpressionSubqueryTupleElementMapper(Type<?> type, SubqueryProviderFactory providerFactory, String subqueryExpression, String subqueryAlias, String attributePath, String viewPath, String embeddingViewPath, String alias) {
        super(type, providerFactory, subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewPath);
        this.alias = alias;
    }

    @Override
    protected SubqueryInitiator<?> subqueryInitiator(SelectBuilder<?> queryBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(viewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        SubqueryInitiator<?> subqueryInitiator;
        if (asString && basicTypeStringSupport != null) {
            subqueryInitiator = queryBuilder.selectSubquery(subqueryAlias, basicTypeStringSupport.toStringExpression(subqueryExpression), alias);
        } else {
            subqueryInitiator = queryBuilder.selectSubquery(subqueryAlias, subqueryExpression, alias);
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
        return subqueryInitiator;
    }

    @Override
    public String getAlias() {
        return alias;
    }
}
