/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AliasExpressionSubqueryTupleElementMapper extends ExpressionSubqueryTupleElementMapper implements AliasedTupleElementMapper {

    private final String alias;

    public AliasExpressionSubqueryTupleElementMapper(Type<?> type, SubqueryProvider provider, String subqueryExpression, String subqueryAlias, String attributePath, String viewPath, String embeddingViewPath, String alias) {
        super(type, provider, subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewPath);
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
