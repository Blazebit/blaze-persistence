/*
 * Copyright 2014 - 2018 Blazebit.
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
import com.blazebit.persistence.view.impl.SubqueryProviderFactory;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterizedAliasSubqueryTupleElementMapper extends ParameterizedSubqueryTupleElementMapper {

    private final String alias;

    public ParameterizedAliasSubqueryTupleElementMapper(SubqueryProviderFactory providerFactory, String embeddingViewPath, String alias) {
        super(providerFactory, embeddingViewPath);
        this.alias = alias;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        providerFactory.create(parameterHolder, optionalParameters).createSubquery(queryBuilder.selectSubquery(alias));
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
    }

}
