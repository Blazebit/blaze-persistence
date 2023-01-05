/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionDeleteModificationQuerySpecification<T> extends DeleteModificationQuerySpecification<T> {

    private final Map<String, String> columnExpressionRemappings;

    public CollectionDeleteModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames,
                                                          List<String> keyRestrictedLeftJoinAliases, List<EntityFunctionNode> entityFunctionNodes, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes, boolean isEmbedded,
                                                          String[] returningColumns, ReturningObjectBuilder<T> objectBuilder, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, boolean queryPlanCacheEnabled,
                                                          String tableToDelete, String tableAlias, String[] idColumns, boolean innerJoinOnly, Query deleteExampleQuery, Map<String, String> columnExpressionRemappings) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, keyRestrictedLeftJoinAliases, entityFunctionNodes, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, objectBuilder, includedModificationStates, returningAttributeBindingMap, queryPlanCacheEnabled, tableToDelete, tableAlias, idColumns, innerJoinOnly, deleteExampleQuery);
        this.columnExpressionRemappings = columnExpressionRemappings;
    }

    @Override
    protected StringBuilder applyCtes(StringBuilder sqlSb, Query baseQuery, List<Query> participatingQueries) {
        SqlUtils.remapColumnExpressions(sqlSb, columnExpressionRemappings);
        return super.applyCtes(sqlSb, baseQuery, participatingQueries);
    }

}
