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

import com.blazebit.persistence.BaseDeleteCriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.function.colldml.CollectionDmlSupportFunction;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CollectionDeleteModificationQuerySpecification;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningCollectionDeleteModificationQuerySpecification;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JoinTable;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
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
    protected final Type<?> elementType;
    protected final ExtendedAttribute<?, ?> collectionAttribute;

    public AbstractDeleteCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, alias, cteKey, cteClass, result, listener);
        this.collectionName = collectionName;
        ExtendedManagedType extendedManagedType = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, entityType);
        this.collectionAttribute = extendedManagedType.getAttribute(collectionName);
        // Add the join here so that references in the where clause goes the the expected join node
        // Also, this validates the collection actually exists
        JoinNode join = joinManager.join(entityAlias + "." + collectionName, CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS, JoinType.LEFT, false, true, null);
        if (collectionAttribute.getJoinTable() != null) {
            join.setDeReferenceFunction(mainQuery.jpaProvider.getCustomFunctionInvocation(CollectionDmlSupportFunction.FUNCTION_NAME, 1));
            join.getParent().setDeReferenceFunction(mainQuery.jpaProvider.getCustomFunctionInvocation(CollectionDmlSupportFunction.FUNCTION_NAME, 1));
        }
        this.elementType = join.getType();
        if (collectionAttribute.getJoinTable() == null && "".equals(collectionAttribute.getMappedBy())) {
            throw new IllegalArgumentException("Unsupported collection attribute that doesn't have a join table or a mapped by attribute!");
        }

        if (collectionAttribute.getMappedBy() != null) {
            // Use a different alias to properly prefix paths with the collection role alias
            JoinNode rootNode = joinManager.getRootNodeOrFail(null);
            rootNode.getAliasInfo().setAlias(CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS + "." + collectionAttribute.getMappedBy());
        }
    }

    public AbstractDeleteCollectionCriteriaBuilder(AbstractDeleteCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
        this.collectionName = builder.collectionName;
        this.collectionAttribute = builder.collectionAttribute;
        this.elementType = builder.elementType;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode) {
        if (externalRepresentation) {
            sbSelectFrom.append("DELETE FROM ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append('(').append(collectionName).append(") ");
            sbSelectFrom.append(entityAlias);
            appendWhereClause(sbSelectFrom, externalRepresentation);
        } else if (collectionAttribute.getJoinTable() == null) {
            sbSelectFrom.append("DELETE FROM ");
            sbSelectFrom.append(((EntityType<?>) elementType).getName());
            sbSelectFrom.append(' ');
            sbSelectFrom.append(CollectionDeleteModificationQuerySpecification.COLLECTION_BASE_QUERY_ALIAS);
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
        if (collectionAttribute.getJoinTable() == null) {
            return super.getQuery(includedModificationStates);
        } else {
            Query baseQuery = em.createQuery(getBaseQueryStringWithCheck(null, null));
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
    }

    @Override
    protected <R> TypedQuery<ReturningResult<R>> getExecuteWithReturningQuery(TypedQuery<Object[]> exampleQuery, Query baseQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder) {
        if (collectionAttribute.getJoinTable() == null) {
            return super.getExecuteWithReturningQuery(exampleQuery, baseQuery, returningColumns, objectBuilder);
        } else {
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
        JoinTable joinTable = collectionAttribute.getJoinTable();
        if (joinTable == null) {
            throw new IllegalStateException("Deleting inverse collections is not supported!");
        }
        int joinTableIndex = SqlUtils.indexOfTableName(sql, joinTable.getTableName());
        String collectionAlias = SqlUtils.extractAlias(sql, joinTableIndex + joinTable.getTableName().length());

        String deleteSql = "delete from " + joinTable.getTableName();
        Map<String, String> columnExpressionRemappings = new HashMap<>(joinTable.getIdColumnMappings().size());
        if (joinTable.getKeyColumnMappings() != null) {
            for (Map.Entry<String, String> entry : joinTable.getKeyColumnMappings().entrySet()) {
                columnExpressionRemappings.put(CollectionDmlSupportFunction.FUNCTION_NAME + "(" + collectionAlias + "." + entry.getValue() + ")", joinTable.getTableName() + "." + entry.getKey());
            }
        }

        String[] discriminatorColumnCheck = mainQuery.jpaProvider.getDiscriminatorColumnCheck(entityType);
        if (discriminatorColumnCheck != null) {
            columnExpressionRemappings.put(ownerAlias + "." + discriminatorColumnCheck[0] + "=" + discriminatorColumnCheck[1], "1=1");
        }
        for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
            columnExpressionRemappings.put(CollectionDmlSupportFunction.FUNCTION_NAME + "(" + ownerAlias + "." + entry.getValue() + ")", joinTable.getTableName() + "." + entry.getKey());
        }
        for (Map.Entry<String, String> entry : joinTable.getTargetColumnMappings().entrySet()) {
            columnExpressionRemappings.put(CollectionDmlSupportFunction.FUNCTION_NAME + "(" + targetAlias + "." + entry.getValue() + ")", joinTable.getTableName() + "." + entry.getKey());
        }
        // If the id attribute is an embedded type, there is the possibility that row value expressions are used which we need to handle as well
        Set<SingularAttribute<?, ?>> idAttributes = JpaMetamodelUtils.getIdAttributes(entityType);
        if (idAttributes.size() == 1 && idAttributes.iterator().next().getType() instanceof ManagedType<?>) {
            StringBuilder leftSb = new StringBuilder();
            StringBuilder rightSb = new StringBuilder();
            leftSb.append(CollectionDmlSupportFunction.FUNCTION_NAME).append("((");
            rightSb.append("(");
            for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
                leftSb.append(ownerAlias).append('.').append(entry.getValue()).append(", ");
                rightSb.append(joinTable.getTableName()).append('.').append(entry.getKey()).append(',');
            }
            leftSb.setLength(leftSb.length() - 2);
            leftSb.append("))");
            rightSb.setCharAt(rightSb.length() - 1, ')');
            columnExpressionRemappings.put(leftSb.toString(), rightSb.toString());
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
