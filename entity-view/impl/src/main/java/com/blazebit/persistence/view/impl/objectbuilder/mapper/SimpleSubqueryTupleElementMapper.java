/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.Type;
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
public class SimpleSubqueryTupleElementMapper implements SubqueryTupleElementMapper {

    protected final BasicUserTypeStringSupport<Object> basicTypeStringSupport;
    protected final SubqueryProvider provider;
    protected final String attributePath;
    protected final String viewPath;
    protected final String embeddingViewPath;

    public SimpleSubqueryTupleElementMapper(Type<?> type, SubqueryProvider provider, String attributePath, String viewPath, String embeddingViewPath) {
        this.basicTypeStringSupport = TypeUtils.forType(type);
        this.provider = provider;
        this.attributePath = attributePath;
        this.viewPath = viewPath;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        if (asString && basicTypeStringSupport != null) {
            provider.createSubquery(queryBuilder.selectSubquery("alias", basicTypeStringSupport.toStringExpression("alias")));
        } else {
            provider.createSubquery(queryBuilder.selectSubquery());
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
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
        return null;
    }

    @Override
    public String getSubqueryExpression() {
        return null;
    }

    @Override
    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport() {
        return basicTypeStringSupport;
    }
}
