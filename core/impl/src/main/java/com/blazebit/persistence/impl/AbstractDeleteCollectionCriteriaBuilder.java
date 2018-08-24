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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseDeleteCriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CollectionDeleteModificationQuerySpecification;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningCollectionDeleteModificationQuerySpecification;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractDeleteCollectionCriteriaBuilder<T, X extends BaseDeleteCriteriaBuilder<T, X>, Y> extends BaseDeleteCriteriaBuilderImpl<T, X, Y> {

    protected final String collectionName;

    public AbstractDeleteCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, alias, cteName, cteClass, result, listener);
        this.collectionName = collectionName;
        // Add the join here so that references in the where clause go the the expected join node
        // Also, this validates the collection actually exists
        joinManager.join(entityAlias + "." + collectionName, CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS, JoinType.LEFT, false, true);
    }

    public AbstractDeleteCollectionCriteriaBuilder(AbstractDeleteCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.collectionName = builder.collectionName;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        if (externalRepresentation) {
            sbSelectFrom.append("DELETE FROM ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append('(').append(collectionName).append(") ");
            sbSelectFrom.append(entityAlias);
            appendWhereClause(sbSelectFrom, externalRepresentation);
        } else {
            // The internal representation is just a "hull" to hold the parameters at the appropriate positions
            sbSelectFrom.append("SELECT 1 FROM ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append(' ');
            sbSelectFrom.append(entityAlias);
            sbSelectFrom.append(" LEFT JOIN ");
            sbSelectFrom.append(entityAlias).append('.').append(collectionName)
                    .append(' ').append(CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
            appendWhereClause(sbSelectFrom, externalRepresentation);
        }
    }

    @Override
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        Query baseQuery = em.createQuery(getBaseQueryStringWithCheck());
        QuerySpecification querySpecification = getQuerySpecification(baseQuery, getCountExampleQuery(), getReturningColumns(), null, includedModificationStates);

        Query query = new CustomSQLQuery(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(query);

        return query;
    }

    @Override
    protected <R> TypedQuery<ReturningResult<R>> getExecuteWithReturningQuery(TypedQuery<Object[]> exampleQuery, Query baseQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder) {
        QuerySpecification querySpecification = getQuerySpecification(baseQuery, exampleQuery, returningColumns, objectBuilder, null);

        CustomReturningSQLTypedQuery query = new CustomReturningSQLTypedQuery<R>(
                querySpecification,
                exampleQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(query);

        return query;
    }

    private <R> QuerySpecification getQuerySpecification(Query baseQuery, Query exampleQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder, Map<DbmsModificationState, String> includedModificationStates) {
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

        boolean isEmbedded = this instanceof ReturningBuilder;
        boolean shouldRenderCteNodes = renderCteNodes(isEmbedded);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(isEmbedded) : Collections.EMPTY_LIST;

        // Prepare a Map<EntityAlias.idColumnName, CollectionAlias.idColumnName>
        // This is used to replace references to id columns properly in the final sql query
        ExtendedQuerySupport extendedQuerySupport = getService(ExtendedQuerySupport.class);
        String sql = extendedQuerySupport.getSql(em, baseQuery);
        String ownerAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, entityAlias);
        String targetAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
        JoinTable joinTable = mainQuery.jpaProvider.getJoinTable(entityType, collectionName);
        if (joinTable == null) {
            throw new IllegalStateException("Deleting inverse collections is not supported!");
        }
        int joinTableIndex = SqlUtils.indexOfTableName(sql, joinTable.getTableName());
        String collectionAlias = SqlUtils.extractAlias(sql, joinTableIndex + joinTable.getTableName().length());

        String deleteSql = "delete from " + joinTable.getTableName();
        Map<String, String> columnExpressionRemappings = new HashMap<>(joinTable.getIdColumnMappings().size());
        columnExpressionRemappings.put(collectionAlias + ".", joinTable.getTableName() + ".");
        for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
            columnExpressionRemappings.put(ownerAlias + "." + entry.getValue(), joinTable.getTableName() + "." + entry.getKey());
        }
        for (Map.Entry<String, String> entry : joinTable.getTargetColumnMappings().entrySet()) {
            columnExpressionRemappings.put(targetAlias + "." + entry.getValue(), joinTable.getTableName() + "." + entry.getKey());
        }

        if (returningColumns == null) {
            return new CollectionDeleteModificationQuerySpecification(
                    this,
                    baseQuery,
                    exampleQuery,
                    parameterManager.getParameters(),
                    parameterListNames,
                    mainQuery.cteManager.isRecursive(),
                    ctes,
                    shouldRenderCteNodes,
                    isEmbedded,
                    returningColumns,
                    includedModificationStates,
                    returningAttributeBindingMap,
                    getDeleteExampleQuery(),
                    deleteSql,
                    columnExpressionRemappings
            );
        } else {
            return new ReturningCollectionDeleteModificationQuerySpecification<>(
                    this,
                    baseQuery,
                    exampleQuery,
                    parameterManager.getParameters(),
                    parameterListNames,
                    mainQuery.cteManager.isRecursive(),
                    ctes,
                    shouldRenderCteNodes,
                    isEmbedded,
                    returningColumns,
                    includedModificationStates,
                    returningAttributeBindingMap,
                    getDeleteExampleQuery(),
                    deleteSql,
                    columnExpressionRemappings,
                    objectBuilder
            );
        }
    }

    protected Query getDeleteExampleQuery() {
        // This is the query we use as "hull" to put other sqls into
        // We chose ValuesEntity as deletion base because it is known to be non-polymorphic
        // We could have used the owner entity type as well, but at the time of writing,
        // it wasn't clear if problems might arise when the entity type were polymorphic
        String exampleQueryString = "DELETE FROM " + ValuesEntity.class.getSimpleName();
        return em.createQuery(exampleQueryString);
    }

}
