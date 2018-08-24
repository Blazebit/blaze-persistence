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

import com.blazebit.persistence.BaseInsertCriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CollectionInsertModificationQuerySpecification;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningCollectionInsertModificationQuerySpecification;
import com.blazebit.persistence.parser.AttributePath;
import com.blazebit.persistence.parser.QualifiedAttribute;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
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
public abstract class AbstractInsertCollectionCriteriaBuilder<T, X extends BaseInsertCriteriaBuilder<T, X>, Y> extends BaseInsertCriteriaBuilderImpl<T, X, Y> {

    private static final String COLLECTION_BASE_QUERY_ALIAS = "_collection";
    private final String collectionName;

    public AbstractInsertCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, cteName, cteClass, result, listener);
        this.collectionName = collectionName;
        // TODO: validate the collection name exists
    }

    public AbstractInsertCollectionCriteriaBuilder(AbstractInsertCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.collectionName = builder.collectionName;
    }

    @Override
    protected void appendInsertIntoFragment(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        super.appendInsertIntoFragment(sbSelectFrom, externalRepresentation);
        if (externalRepresentation) {
            sbSelectFrom.append('.').append(collectionName);
        }
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        if (externalRepresentation) {
            super.buildBaseQueryString(sbSelectFrom, externalRepresentation);
        } else {
            buildSelectBaseQueryString(sbSelectFrom, externalRepresentation);
        }
    }

    @Override
    protected void addBind(String attributeName) {
        AttributePath attributePath = JpaMetamodelUtils.getJoinTableCollectionAttributePath(getMetamodel(), entityType, attributeName, collectionName);
        StringBuilder sb = new StringBuilder();
        List<Attribute<?, ?>> attributes = attributePath.getAttributes();
        Attribute<?, ?> attribute = attributes.get(0);
        // Replace the collection name with the alias for easier processing
        if (attribute instanceof QualifiedAttribute) {
            sb.append(((QualifiedAttribute) attribute).getQualificationExpression());
            sb.append('(');
            sb.append(COLLECTION_BASE_QUERY_ALIAS);
            sb.append(')');
        } else if (collectionName.equals(attribute.getName())) {
            sb.append(COLLECTION_BASE_QUERY_ALIAS);
        } else {
            sb.append(entityAlias).append('.');
            sb.append(attribute.getName());
        }
        for (int i = 1; i < attributes.size(); i++) {
            attribute = attributes.get(i);
            sb.append('.');
            sb.append(attribute.getName());
        }
        attributeName = sb.toString();
        Integer attributeBindIndex = bindingMap.get(attributeName);

        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }

        bindingMap.put(attributeName, selectManager.getSelectInfos().size());
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

        baseQuery.setFirstResult(firstResult);
        baseQuery.setMaxResults(maxResults);

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

        baseQuery.setFirstResult(firstResult);
        baseQuery.setMaxResults(maxResults);

        return query;
    }

    private <R> QuerySpecification getQuerySpecification(Query baseQuery, Query exampleQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder, Map<DbmsModificationState, String> includedModificationStates) {
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, Collections.EMPTY_SET);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);

        boolean isEmbedded = this instanceof ReturningBuilder;
        boolean shouldRenderCteNodes = renderCteNodes(isEmbedded);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(isEmbedded) : Collections.EMPTY_LIST;

        ExtendedQuerySupport extendedQuerySupport = getService(ExtendedQuerySupport.class);

        Query insertExampleQuery = getInsertExampleQuery();
        String insertExampleSql = extendedQuerySupport.getSql(em, insertExampleQuery);
        String ownerAlias = extendedQuerySupport.getSqlAlias(em, insertExampleQuery, entityAlias);
        String targetAlias = extendedQuerySupport.getSqlAlias(em, insertExampleQuery, COLLECTION_BASE_QUERY_ALIAS);
        JoinTable joinTable = mainQuery.jpaProvider.getJoinTable(entityType, collectionName);
        int joinTableIndex = SqlUtils.indexOfTableName(insertExampleSql, joinTable.getTableName());
        String collectionAlias = SqlUtils.extractAlias(insertExampleSql, joinTableIndex + joinTable.getTableName().length());
        String[] selectItemExpressions = SqlUtils.getSelectItemExpressions(insertExampleSql, SqlUtils.indexOfSelect(insertExampleSql));

        // Prepare a Map<EntityAlias.idColumnName, CollectionAlias.idColumnName>
        // This is used to replace references to id columns properly in the final sql query
        Map<String, String> columnExpressionRemappings = new HashMap<>(selectItemExpressions.length);

        if (joinTable.getKeyColumnMappings() != null) {
            for (Map.Entry<String, String> entry : joinTable.getKeyColumnMappings().entrySet()) {
                columnExpressionRemappings.put(collectionAlias + "." + entry.getValue(), entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
            columnExpressionRemappings.put(ownerAlias + "." + entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, String> entry : joinTable.getTargetColumnMappings().entrySet()) {
            columnExpressionRemappings.put(targetAlias + "." + entry.getValue(), entry.getKey());
        }

        StringBuilder insertSqlSb = new StringBuilder();
        insertSqlSb.append("insert into ").append(joinTable.getTableName()).append("(");
        for (String selectItemExpression : selectItemExpressions) {
            insertSqlSb.append(columnExpressionRemappings.get(selectItemExpression.trim())).append(',');
        }
        insertSqlSb.setCharAt(insertSqlSb.length() - 1, ')');

        if (returningColumns == null) {
            return new CollectionInsertModificationQuerySpecification(
                    this,
                    baseQuery,
                    exampleQuery,
                    parameterManager.getParameters(),
                    parameterListNames,
                    keyRestrictedLeftJoinAliases,
                    entityFunctionNodes,
                    mainQuery.cteManager.isRecursive(),
                    ctes,
                    shouldRenderCteNodes,
                    isEmbedded,
                    returningColumns,
                    includedModificationStates,
                    returningAttributeBindingMap,
                    getInsertExecutorQuery(),
                    insertSqlSb.toString()
            );
        } else {
            return new ReturningCollectionInsertModificationQuerySpecification(
                    this,
                    baseQuery,
                    exampleQuery,
                    parameterManager.getParameters(),
                    parameterListNames,
                    keyRestrictedLeftJoinAliases,
                    entityFunctionNodes,
                    mainQuery.cteManager.isRecursive(),
                    ctes,
                    shouldRenderCteNodes,
                    isEmbedded,
                    returningColumns,
                    includedModificationStates,
                    returningAttributeBindingMap,
                    getInsertExecutorQuery(),
                    insertSqlSb.toString(),
                    objectBuilder
            );
        }
    }

    protected Query getInsertExampleQuery() {
        // We produce this query just to extract the select expressions for the bindings
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        for (Map.Entry<String, Integer> entry : bindingMap.entrySet()) {
            sb.append(entry.getKey());
            sb.append(',');
        }

        sb.setCharAt(sb.length() - 1, ' ');
        sb.append("FROM ");
        sb.append(entityType.getName());
        sb.append(' ');
        sb.append(entityAlias);
        sb.append(" LEFT JOIN ");
        sb.append(entityAlias).append('.').append(collectionName)
                .append(' ').append(COLLECTION_BASE_QUERY_ALIAS);
        return em.createQuery(sb.toString());
    }

    protected Query getInsertExecutorQuery() {
        // This is the query we use as "hull" to put other sqls into
        // We chose ValuesEntity as insert base because it is known to be non-polymorphic
        // We could have used the owner entity type as well, but at the time of writing,
        // it wasn't clear if problems might arise when the entity type were polymorphic
        String exampleQueryString = "UPDATE " + ValuesEntity.class.getSimpleName() + " SET value = NULL";
        return em.createQuery(exampleQueryString);
    }

}
