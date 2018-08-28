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

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.parser.AttributePath;
import com.blazebit.persistence.parser.QualifiedAttribute;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CollectionUpdateModificationQuerySpecification;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningCollectionUpdateModificationQuerySpecification;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
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
public abstract class AbstractUpdateCollectionCriteriaBuilder<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends BaseUpdateCriteriaBuilderImpl<T, X, Y> {

    private final String collectionName;

    private List<String> cachedBaseQueryStrings;

    public AbstractUpdateCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, alias, cteName, cteClass, result, listener);
        this.collectionName = collectionName;
        // Add the join here so that references in the where clause go the the expected join node
        // Also, this validates the collection actually exists
        joinManager.join(entityAlias + "." + collectionName, CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS, JoinType.LEFT, false, true);
    }

    public AbstractUpdateCollectionCriteriaBuilder(AbstractUpdateCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.collectionName = builder.collectionName;
    }

    @Override
    protected String checkAttribute(String attributeName) {
        // Assert the attribute exists and "clean" the attribute path
        AttributePath attributePath = JpaMetamodelUtils.getJoinTableCollectionAttributePath(getMetamodel(), entityType, attributeName, collectionName);
        StringBuilder sb = new StringBuilder();
        for (Attribute<?, ?> attribute : attributePath.getAttributes()) {
            // Replace the collection name with the alias for easier processing
            if (attribute instanceof QualifiedAttribute) {
                sb.append(((QualifiedAttribute) attribute).getQualificationExpression());
                sb.append('(');
                sb.append(CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
                sb.append(')');
            } else if (collectionName.equals(attribute.getName())) {
                sb.append(CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
            } else {
                sb.append(attribute.getName());
            }
            sb.append('.');
        }
        attributeName = sb.substring(0, sb.length() - 1);
        Expression attributeExpression = setAttributes.get(attributeName);

        if (attributeExpression != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }
        return attributeName;
    }

    @Override
    protected void prepareForModification(ClauseType changedClause) {
        super.prepareForModification(changedClause);
        cachedBaseQueryStrings = null;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        if (externalRepresentation) {
            sbSelectFrom.append("UPDATE ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append('(').append(collectionName).append(") ");
            sbSelectFrom.append(entityAlias);
            appendSetClause(sbSelectFrom);
            appendWhereClause(sbSelectFrom, externalRepresentation);
        } else {
            // The internal representation is just a "hull" to hold the parameters at the appropriate positions
            sbSelectFrom.append("SELECT 1 FROM ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append(' ');
            sbSelectFrom.append(entityAlias);
            sbSelectFrom.append(" LEFT JOIN ");
            sbSelectFrom.append(entityAlias).append('.').append(collectionName)
                    .append(' ').append(CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
            appendWhereClause(sbSelectFrom, externalRepresentation);

            // Create the select query strings that are used for the set items
            // The idea is to encode a set item as an equality predicate in a dedicated query
            // Each query will then participate in the overall query as parameter container
            // and the extracted equality predicates can be used as-is for the set clause
            cachedBaseQueryStrings = new ArrayList<>();
            StringBuilder sbSetExpressionQuery = new StringBuilder();

            for (Map.Entry<String, Expression> attributeEntry : setAttributes.entrySet()) {
                fillCachedBaseQueryStrings(sbSetExpressionQuery, attributeEntry.getKey(), attributeEntry.getValue());
            }
        }
    }

    private void fillCachedBaseQueryStrings(StringBuilder sbSetExpressionQuery, String attributePath, Expression value) {
        sbSetExpressionQuery.setLength(0);
        StringBuilder oldBuffer = queryGenerator.getQueryBuffer();
        queryGenerator.setClauseType(ClauseType.SET);
        queryGenerator.setQueryBuffer(sbSetExpressionQuery);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        sbSetExpressionQuery.append("SELECT 1 FROM ");
        sbSetExpressionQuery.append(entityType.getName());
        sbSetExpressionQuery.append(' ');
        sbSetExpressionQuery.append(entityAlias);
        sbSetExpressionQuery.append(" LEFT JOIN ");
        sbSetExpressionQuery.append(entityAlias).append('.').append(collectionName)
                .append(' ').append(CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
        sbSetExpressionQuery.append(" WHERE ");
        sbSetExpressionQuery.append(attributePath).append('=');
        value.accept(queryGenerator);
        cachedBaseQueryStrings.add(sbSetExpressionQuery.toString());

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
        queryGenerator.setQueryBuffer(oldBuffer);
    }

    @Override
    protected boolean appendSetElementEntityPrefix(String trimmedPath) {
        // Prevent collection aliases to be prefixed
        return !trimmedPath.startsWith(CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS) && super.appendSetElementEntityPrefix(trimmedPath);
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

        ExtendedQuerySupport extendedQuerySupport = getService(ExtendedQuerySupport.class);
        String sql = extendedQuerySupport.getSql(em, baseQuery);
        String ownerAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, entityAlias);
        String targetAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, CollectionUpdateModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
        JoinTable joinTable = mainQuery.jpaProvider.getJoinTable(entityType, collectionName);
        int joinTableIndex = SqlUtils.indexOfTableName(sql, joinTable.getTableName());
        String collectionAlias = SqlUtils.extractAlias(sql, joinTableIndex + joinTable.getTableName().length());

        String updateSql = "update " + joinTable.getTableName() + " set ";

        // Prepare a Map<EntityAlias.idColumnName, CollectionAlias.idColumnName>
        // This is used to replace references to id columns properly in the final sql query
        Map<String, String> columnOnlyRemappings = new HashMap<>();
        Map<String, String> columnExpressionRemappings = new HashMap<>();

        if (joinTable.getKeyColumnMappings() != null) {
            for (Map.Entry<String, String> entry : joinTable.getKeyColumnMappings().entrySet()) {
                columnOnlyRemappings.put(collectionAlias + "." + entry.getValue(), entry.getKey());
                columnExpressionRemappings.put(collectionAlias + "." + entry.getValue(), joinTable.getTableName() + "." + entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
            columnOnlyRemappings.put(ownerAlias + "." + entry.getValue(), entry.getKey());
            columnExpressionRemappings.put(ownerAlias + "." + entry.getValue(), joinTable.getTableName() + "." + entry.getKey());
        }
        for (Map.Entry<String, String> entry : joinTable.getTargetColumnMappings().entrySet()) {
            columnOnlyRemappings.put(targetAlias + "." + entry.getValue(), entry.getKey());
            columnExpressionRemappings.put(targetAlias + "." + entry.getValue(), joinTable.getTableName() + "." + entry.getKey());
        }

        List<Query> setExpressionContainingUpdateQueries = new ArrayList<>();

        for (String cachedBaseQueryString : cachedBaseQueryStrings) {
            Query setExpressionQuery = em.createQuery(cachedBaseQueryString);
            parameterListNames.addAll(parameterManager.getParameterListNames(baseQuery));
            setExpressionContainingUpdateQueries.add(setExpressionQuery);
        }

        if (returningColumns == null) {
            return new CollectionUpdateModificationQuerySpecification(
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
                    getUpdateExampleQuery(),
                    updateSql,
                    setExpressionContainingUpdateQueries,
                    columnOnlyRemappings,
                    columnExpressionRemappings
            );
        } else {
            return new ReturningCollectionUpdateModificationQuerySpecification(
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
                    getUpdateExampleQuery(),
                    updateSql,
                    setExpressionContainingUpdateQueries,
                    columnOnlyRemappings,
                    columnExpressionRemappings,
                    objectBuilder
            );
        }
    }

    protected Query getUpdateExampleQuery() {
        // This is the query we use as "hull" to put other sqls into
        // We chose ValuesEntity as update base because it is known to be non-polymorphic
        // We could have used the owner entity type as well, but at the time of writing,
        // it wasn't clear if problems might arise when the entity type were polymorphic
        String exampleQueryString = "UPDATE " + ValuesEntity.class.getSimpleName() + " SET value = NULL";
        return em.createQuery(exampleQueryString);
    }

}
