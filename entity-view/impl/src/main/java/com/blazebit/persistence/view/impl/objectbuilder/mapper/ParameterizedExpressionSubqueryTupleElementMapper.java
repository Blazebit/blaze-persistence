/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterizedExpressionSubqueryTupleElementMapper implements SubqueryTupleElementMapper {

    protected final BasicUserTypeStringSupport<Object> basicTypeStringSupport;
    protected final SubqueryProviderFactory providerFactory;
    protected final String subqueryExpression;
    protected final String subqueryAlias;
    protected final String attributePath;
    protected final String viewPath;
    protected final String embeddingViewPath;

    public ParameterizedExpressionSubqueryTupleElementMapper(Type<?> type, SubqueryProviderFactory providerFactory, String subqueryExpression, String subqueryAlias, String attributePath, String viewPath, String embeddingViewPath) {
        this.basicTypeStringSupport = TypeUtils.forType(type);
        this.providerFactory = providerFactory;
        this.subqueryExpression = subqueryExpression;
        this.subqueryAlias = subqueryAlias;
        this.attributePath = attributePath;
        this.viewPath = viewPath;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(null);
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        providerFactory.create(parameterHolder, optionalParameters).createSubquery(subqueryInitiator(queryBuilder, viewJpqlMacro, embeddingViewJpqlMacro, asString));
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
