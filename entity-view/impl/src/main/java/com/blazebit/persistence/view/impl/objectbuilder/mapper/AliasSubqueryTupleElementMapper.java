/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AliasSubqueryTupleElementMapper extends SimpleSubqueryTupleElementMapper implements AliasedTupleElementMapper {

    private final String alias;

    public AliasSubqueryTupleElementMapper(Type<?> type, SubqueryProvider provider, String attributePath, String viewPath, String embeddingViewPath, String alias) {
        super(type, provider, attributePath, viewPath, embeddingViewPath);
        this.alias = alias;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        if (asString && basicTypeStringSupport != null) {
            provider.createSubquery(queryBuilder.selectSubquery(alias, basicTypeStringSupport.toStringExpression(alias)));
        } else {
            provider.createSubquery(queryBuilder.selectSubquery(alias));
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
    }

    @Override
    public String getAlias() {
        return alias;
    }
}
