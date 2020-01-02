/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractInsertCollectionCriteriaBuilder<T, X extends BaseInsertCriteriaBuilder<T, X>, Y> extends BaseInsertCriteriaBuilderImpl<T, X, Y> {

    private static final String COLLECTION_BASE_QUERY_ALIAS = "_collection";
    private final String collectionName;
    private final String keyFunctionExpression;
    private final Map<String, ExtendedAttribute<?, ?>> collectionAttributeEntries;
    private final Map<String, String> collectionColumnBindingMap;
    private final Type<?> elementType;
    private final ExtendedAttribute<?, ?> collectionAttribute;

    public AbstractInsertCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, cteName, cteClass, result, listener);
        this.collectionName = collectionName;
        ExtendedManagedType extendedManagedType = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, entityType);
        this.collectionAttribute = extendedManagedType.getAttribute(collectionName);
        this.elementType = mainQuery.metamodel.type(collectionAttribute.getElementClass());
        if (collectionAttribute.getJoinTable() == null && "".equals(collectionAttribute.getMappedBy())) {
            throw new IllegalArgumentException("Unsupported collection attribute that doesn't have a join table or a mapped by attribute!");
        }
        if (collectionAttribute.getMappedBy() != null) {
            throw new IllegalArgumentException("Insert operation on inverse collections is currently not supported!");
        }
        Map<String, ExtendedAttribute<?, ?>> collectionAttributeEntries = JpaUtils.getCollectionAttributeEntries(mainQuery.metamodel, entityType, collectionAttribute);
        if (collectionAttribute.getAttribute() instanceof MapAttribute<?, ?, ?>) {
            keyFunctionExpression = "key(" + collectionName + ")";
        } else if (collectionAttribute.getAttribute() instanceof ListAttribute<?, ?> && !collectionAttribute.isBag()) {
            keyFunctionExpression = "index(" + collectionName + ")";
        } else {
            keyFunctionExpression = null;
        }
        this.collectionColumnBindingMap = new LinkedHashMap<>(collectionAttributeEntries.size());
        this.collectionAttributeEntries = collectionAttributeEntries;
    }

    public AbstractInsertCollectionCriteriaBuilder(AbstractInsertCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.collectionName = builder.collectionName;
        this.keyFunctionExpression = builder.keyFunctionExpression;
        this.collectionColumnBindingMap = builder.collectionColumnBindingMap;
        this.collectionAttributeEntries = builder.collectionAttributeEntries;
        this.collectionAttribute = builder.collectionAttribute;
        this.elementType = builder.elementType;
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
        if (attributeName.equalsIgnoreCase(keyFunctionExpression)) {
            Integer attributeBindIndex = bindingMap.get(attributeName);

            if (attributeBindIndex != null) {
                throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
            }

            bindingMap.put(attributeName, selectManager.getSelectInfos().size());
            return;
        }
        ExtendedAttribute attributeEntry = collectionAttributeEntries.get(attributeName);
        if (attributeEntry == null) {
            Set<String> set = new TreeSet<>(collectionAttributeEntries.keySet());
            if (keyFunctionExpression != null) {
                set.add(keyFunctionExpression);
            }
            throw new IllegalArgumentException("The attribute [" + attributeName + "] does not exist or can't be bound! Allowed attributes are: " + set);
        }

        Integer attributeBindIndex = bindingMap.get(attributeName);

        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }

        bindingMap.put(attributeName, selectManager.getSelectInfos().size());
    }

    @Override
    protected void expandBindings() {
        JpaUtils.expandBindings(entityType, bindingMap, collectionColumnBindingMap, collectionAttributeEntries, ClauseType.SELECT, this);
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

        String[] discriminatorColumnCheck = mainQuery.jpaProvider.getDiscriminatorColumnCheck(entityType);
        if (discriminatorColumnCheck != null) {
            columnExpressionRemappings.put(ownerAlias + "." + discriminatorColumnCheck[0] + "=" + discriminatorColumnCheck[1], "1=1");
        }
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

        int cutoffColumns = 0;
        StringBuilder insertSqlSb = new StringBuilder();
        insertSqlSb.append("insert into ").append(joinTable.getTableName()).append("(");
        for (String selectItemExpression : selectItemExpressions) {
            String columnExpression = columnExpressionRemappings.get(selectItemExpression.trim());
            // It should never be null, but the workaround for https://hibernate.atlassian.net/browse/HHH-13045 requires us to filter out the entity fetch columns
            if (columnExpression == null) {
                cutoffColumns++;
            } else {
                insertSqlSb.append(columnExpression).append(',');
            }
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
                    insertSqlSb.toString(),
                    cutoffColumns
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
                    cutoffColumns,
                    objectBuilder
            );
        }
    }

    protected Query getInsertExampleQuery() {
        // We produce this query just to extract the select expressions for the bindings
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        for (Map.Entry<String, Integer> entry : bindingMap.entrySet()) {
            String expression = entry.getKey();
            int collectionIndex = expression.indexOf(collectionName);
            if (collectionIndex == -1) {
                sb.append(entityAlias).append('.').append(expression);
            } else {
                sb.append(expression, 0, collectionIndex);
                sb.append(COLLECTION_BASE_QUERY_ALIAS);
                sb.append(expression, collectionIndex + collectionName.length(), expression.length());
            }
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
