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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.plan.CustomReturningModificationQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.DbmsModificationState;

import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReturningCollectionUpdateModificationQuerySpecification<T> extends CollectionUpdateModificationQuerySpecification {

    private final ReturningObjectBuilder<T> objectBuilder;

    public ReturningCollectionUpdateModificationQuerySpecification(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> commonQueryBuilder, Query baseQuery, Query exampleQuery, Set<String> parameterListNames, boolean recursive, List<CTENode> ctes, boolean shouldRenderCteNodes,
                                                                   boolean isEmbedded, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates, Map<String, String> returningAttributeBindingMap, Query updateExampleQuery, String updateSql, List<Query> setExpressionContainingUpdateQueries,
                                                                   Map<String, String> columnOnlyRemappings, Map<String, String> columnExpressionRemappings, ReturningObjectBuilder<T> objectBuilder) {
        super(commonQueryBuilder, baseQuery, exampleQuery, parameterListNames, recursive, ctes, shouldRenderCteNodes, isEmbedded, returningColumns, includedModificationStates, returningAttributeBindingMap, updateExampleQuery, updateSql, setExpressionContainingUpdateQueries, columnOnlyRemappings, columnExpressionRemappings);
        this.objectBuilder = objectBuilder;
    }

    @Override
    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults) {
        final String sql = getSql();
        return new CustomReturningModificationQueryPlan<T>(extendedQuerySupport, serviceProvider, exampleQuery, objectBuilder, participatingQueries, sql, firstResult, maxResults, returningColumns.length == 1 && objectBuilder != null);
    }

    @Override
    public SelectQueryPlan createSelectPlan(int firstResult, int maxResults) {
        final String sql = getSql();
        return new CustomReturningModificationQueryPlan<T>(extendedQuerySupport, serviceProvider, exampleQuery, objectBuilder, participatingQueries, sql, firstResult, maxResults, returningColumns.length == 1 && objectBuilder != null);
    }

}
