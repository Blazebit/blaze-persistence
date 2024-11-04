/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;

import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
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
