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
import com.blazebit.persistence.view.SubqueryProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AliasExpressionSubqueryTupleElementMapper extends ExpressionSubqueryTupleElementMapper {

    private final String alias;

    public AliasExpressionSubqueryTupleElementMapper(SubqueryProvider provider, String subqueryExpression, String subqueryAlias, String alias) {
        super(provider, subqueryExpression, subqueryAlias);
        this.alias = alias;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
        provider.createSubquery(queryBuilder.selectSubquery(subqueryAlias, subqueryExpression, alias));
    }

}
