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

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterizedSubqueryTupleElementMapper implements SubqueryTupleElementMapper {

    protected final BasicUserTypeStringSupport<Object> basicTypeStringSupport;
    protected final SubqueryProviderFactory providerFactory;
    protected final String attributePath;
    protected final String viewPath;
    protected final String embeddingViewPath;

    public ParameterizedSubqueryTupleElementMapper(Type<?> type, SubqueryProviderFactory providerFactory, String attributePath, String viewPath, String embeddingViewPath) {
        this.basicTypeStringSupport = TypeUtils.forType(type);
        this.providerFactory = providerFactory;
        this.attributePath = attributePath;
        this.viewPath = viewPath;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(viewPath);
        if (asString && basicTypeStringSupport != null) {
            providerFactory.create(parameterHolder, optionalParameters).createSubquery(queryBuilder.selectSubquery("alias", basicTypeStringSupport.toStringExpression("alias")));
        } else {
            providerFactory.create(parameterHolder, optionalParameters).createSubquery(queryBuilder.selectSubquery());
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
