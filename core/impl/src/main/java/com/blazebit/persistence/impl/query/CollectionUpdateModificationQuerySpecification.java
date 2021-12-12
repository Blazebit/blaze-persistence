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
public class CollectionUpdateModificationQuerySpecification<T> extends UpdateModificationQuerySpecification<T> {

    private final Map<String, String> columnExpressionRemappings;

    public CollectionUpdateModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Collection<? extends Parameter<?>> parameters, Set<String> parameterListNames, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                                          boolean isEmbedded, String[] returningColumns, ReturningObjectBuilder<T> objectBuilder, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, boolean queryPlanCacheEnabled,
                                                          String tableToUpdate, String tableAlias, String[] idColumns, List<String> setColumns, Collection<Query> foreignKeyParticipatingQueries, Map<String, String> aliasMapping, Query updateExampleQuery, Map<String, String> columnExpressionRemappings) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameters, parameterListNames, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, objectBuilder, includedModificationStates, returningAttributeBindingMap, queryPlanCacheEnabled, tableToUpdate, tableAlias, idColumns, setColumns, foreignKeyParticipatingQueries, aliasMapping, updateExampleQuery);
        this.columnExpressionRemappings = columnExpressionRemappings;
    }

    @Override
    protected StringBuilder applyCtes(StringBuilder sqlSb, Query baseQuery, List<Query> participatingQueries) {
        SqlUtils.remapColumnExpressions(sqlSb, columnExpressionRemappings);
        return super.applyCtes(sqlSb, baseQuery, participatingQueries);
    }
}
