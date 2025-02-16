/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.FetchBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AliasExpressionTupleElementMapper extends ExpressionTupleElementMapper implements AliasedTupleElementMapper {

    private final String alias;

    public AliasExpressionTupleElementMapper(BasicUserTypeStringSupport<Object> basicTypeStringSupport, String expression, String alias, String attributePath, String viewPath, String embeddingViewPath, String[] fetches) {
        super(basicTypeStringSupport, expression, attributePath, viewPath, embeddingViewPath, fetches);
        this.alias = alias.intern();
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(viewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        if (asString && basicTypeStringSupport != null) {
            queryBuilder.select( basicTypeStringSupport.toStringExpression( expression ), alias );
        } else {
            queryBuilder.select( expression, alias );
        }
        if (this.fetches.length != 0) {
            final FetchBuilder<?> fetchBuilder = (FetchBuilder<?>) queryBuilder;
            for (int i = 0; i < this.fetches.length; i++) {
                fetchBuilder.fetch(this.fetches[i]);
            }
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
    }

    @Override
    public String getAlias() {
        return alias;
    }
}
