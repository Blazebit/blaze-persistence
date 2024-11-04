/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterizedAliasSubqueryTupleElementMapper extends ParameterizedSubqueryTupleElementMapper implements AliasedTupleElementMapper {

    private final String alias;

    public ParameterizedAliasSubqueryTupleElementMapper(Type<?> type, SubqueryProviderFactory providerFactory, String attributePath, String viewPath, String embeddingViewPath, String alias) {
        super(type, providerFactory, attributePath, viewPath, embeddingViewPath);
        this.alias = alias;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        if (asString && basicTypeStringSupport != null) {
            providerFactory.create(parameterHolder, optionalParameters).createSubquery(queryBuilder.selectSubquery(alias, basicTypeStringSupport.toStringExpression(alias)));
        } else {
            providerFactory.create(parameterHolder, optionalParameters).createSubquery(queryBuilder.selectSubquery(alias));
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
    }

    @Override
    public String getAlias() {
        return alias;
    }
}
