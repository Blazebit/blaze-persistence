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

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.function.colldml.CollectionDmlSupportFunction;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CollectionUpdateModificationQuerySpecification;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningCollectionUpdateModificationQuerySpecification;
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
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
public abstract class AbstractUpdateCollectionCriteriaBuilder<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends BaseUpdateCriteriaBuilderImpl<T, X, Y> {

    private final String collectionName;
    private final String keyFunctionExpression;
    private final Map<String, ExtendedAttribute<?, ?>> collectionAttributeEntries;
    private final Map<String, String> collectionColumnBindingMap;
    private final Type<?> elementType;
    private final ExtendedAttribute<?, ?> collectionAttribute;

    private List<String> cachedBaseQueryStrings;

    public AbstractUpdateCollectionCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, CTEManager.CTEKey cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, isMainQuery, clazz, alias, cteName, cteClass, result, listener);
        this.collectionName = collectionName;
        ExtendedManagedType<?> extendedManagedType = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, entityType);
        this.collectionAttribute = extendedManagedType.getAttribute(collectionName);
        // Add the join here so that references in the where clause go the the expected join node
        // Also, this validates the collection actually exists
        JoinNode join = joinManager.join(entityAlias + "." + collectionName, JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS, JoinType.LEFT, false, true, null);
        join.setDeReferenceFunction(mainQuery.jpaProvider.getCustomFunctionInvocation(CollectionDmlSupportFunction.FUNCTION_NAME, 1));
        join.getParent().setDeReferenceFunction(mainQuery.jpaProvider.getCustomFunctionInvocation(CollectionDmlSupportFunction.FUNCTION_NAME, 1));
        this.elementType = join.getType();
        if (collectionAttribute.getJoinTable() == null && "".equals(collectionAttribute.getMappedBy())) {
            throw new IllegalArgumentException("Cannot update the collection attribute '" + collectionName + "' of entity class '" + clazz.getName() + "' because it doesn't have a join table or a mapped by attribute!");
        }
        if (collectionAttribute.getMappedBy() != null) {
            // Use a different alias to properly prefix paths with the collection role alias
            JoinNode rootNode = joinManager.getRootNodeOrFail(null);
            rootNode.getAliasInfo().setAlias(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS + "." + collectionAttribute.getMappedBy());
        } else {
            JoinTable joinTable = collectionAttribute.getJoinTable();
            Set<String> idAttributeNames = joinTable.getIdAttributeNames();
            Set<String> ownerAttributes = new HashSet<>(idAttributeNames.size());
            for (String idAttributeName : idAttributeNames) {
                ownerAttributes.add(idAttributeName);
                int dotIdx = -1;
                while ((dotIdx = idAttributeName.indexOf('.', dotIdx + 1)) != -1) {
                    ownerAttributes.add(idAttributeName.substring(0, dotIdx));
                }
            }
            join.getParent().setAllowedDeReferences(ownerAttributes);
            join.getParent().setDisallowedDeReferenceAlias(aliasManager.generateRootAlias(join.getParent().getAlias()));

            Set<String> elementAttributes = new HashSet<>();
            if (((PluralAttribute<?, ?, ?>) collectionAttribute.getAttribute()).getElementType() instanceof ManagedType<?>) {
                String prefix = collectionAttribute.getAttributePathString() + ".";
                for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : extendedManagedType.getAttributes().entrySet()) {
                    if (entry.getKey().startsWith(prefix)) {
                        elementAttributes.add(entry.getKey().substring(prefix.length()));
                    }
                }
            }
            join.setAllowedDeReferences(elementAttributes);
            join.setDisallowedDeReferenceAlias(aliasManager.generateRootAlias(join.getAlias()));
        }

        Map<String, ExtendedAttribute<?, ?>> collectionAttributeEntries = JpaUtils.getCollectionAttributeEntries(mainQuery.metamodel, entityType, collectionAttribute);
        if (collectionAttribute.getAttribute() instanceof MapAttribute<?, ?, ?>) {
            keyFunctionExpression = "key(" + collectionName + ")";
        } else if (collectionAttribute.getAttribute() instanceof ListAttribute<?, ?> && !mainQuery.jpaProvider.isBag(entityType, collectionName)) {
            keyFunctionExpression = "index(" + collectionName + ")";
        } else {
            keyFunctionExpression = null;
        }
        this.collectionColumnBindingMap = new LinkedHashMap<>(collectionAttributeEntries.size());
        this.collectionAttributeEntries = collectionAttributeEntries;
    }

    public AbstractUpdateCollectionCriteriaBuilder(AbstractUpdateCollectionCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
        this.collectionName = builder.collectionName;
        this.keyFunctionExpression = builder.keyFunctionExpression;
        this.collectionColumnBindingMap = builder.collectionColumnBindingMap;
        this.collectionAttributeEntries = builder.collectionAttributeEntries;
        this.collectionAttribute = builder.collectionAttribute;
        this.elementType = builder.elementType;
    }

    @Override
    protected void addAttribute(String attributeName) {
        if (attributeName.equalsIgnoreCase(keyFunctionExpression)) {
            Integer attributeBindIndex = setAttributeBindingMap.get(attributeName);

            if (attributeBindIndex != null) {
                throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
            }

            setAttributeBindingMap.put(attributeName, selectManager.getSelectInfos().size());
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

        Integer attributeBindIndex = setAttributeBindingMap.get(attributeName);

        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }

        setAttributeBindingMap.put(attributeName, selectManager.getSelectInfos().size());
    }

    @Override
    protected void prepareForModification(ClauseType changedClause) {
        super.prepareForModification(changedClause);
        cachedBaseQueryStrings = null;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode) {
        JoinNode rootNode = joinManager.getRoots().get(0);
        JoinTreeNode collectionTreeNode = rootNode.getNodes().get(collectionName);
        boolean hasOtherJoinNodes = joinManager.getRoots().size() > 1
                || rootNode.getNodes().size() > 1
                || !rootNode.getTreatedJoinNodes().isEmpty()
                || !rootNode.getEntityJoinNodes().isEmpty()
                || collectionTreeNode.getJoinNodes().size() > 1
                || collectionTreeNode.getDefaultNode().hasChildNodes();
        if (externalRepresentation) {
            sbSelectFrom.append("UPDATE ");
            sbSelectFrom.append(entityType.getName());
            sbSelectFrom.append('(').append(collectionName).append(") ");
            sbSelectFrom.append(entityAlias);
            if (collectionAttribute.getJoinTable() == null) {
                rootNode.getAliasInfo().setAlias(entityAlias);
            }
            collectionTreeNode.getDefaultNode().getAliasInfo().setAlias(entityAlias + "." + collectionName);
            appendSetClause(sbSelectFrom, externalRepresentation);
            if (hasOtherJoinNodes) {
                List<String> whereClauseConjuncts = new ArrayList<>();
                List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), rootNode);
            }
            appendWhereClause(sbSelectFrom, externalRepresentation);
            if (collectionAttribute.getJoinTable() == null) {
                rootNode.getAliasInfo().setAlias(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS + "." + collectionAttribute.getMappedBy());
            }
            collectionTreeNode.getDefaultNode().getAliasInfo().setAlias(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
        } else if (collectionAttribute.getJoinTable() == null) {
            sbSelectFrom.append("UPDATE ");
            sbSelectFrom.append(((EntityType<?>) elementType).getName());
            sbSelectFrom.append(' ');
            sbSelectFrom.append(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
            appendSetClause(sbSelectFrom, externalRepresentation);
            appendWhereClause(sbSelectFrom, externalRepresentation);
        } else {
            // The internal representation is just a "hull" to hold the parameters at the appropriate positions
            sbSelectFrom.append("SELECT 1");
            StringBuilder tempSb = new StringBuilder();
            appendWhereClause(tempSb, externalRepresentation);

            if (hasOtherJoinNodes || rootNode.needsDisallowedDeReferenceAlias(externalRepresentation) || collectionTreeNode.getDefaultNode().needsDisallowedDeReferenceAlias(externalRepresentation)) {
                sbSelectFrom.append(" FROM ");
                sbSelectFrom.append(entityType.getName());
                sbSelectFrom.append(' ');
                sbSelectFrom.append(entityAlias);
                sbSelectFrom.append(" LEFT JOIN ");
                sbSelectFrom.append(entityAlias).append('.').append(collectionName)
                        .append(' ').append(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
                sbSelectFrom.append(" WHERE EXISTS (SELECT 1");
                List<String> whereClauseConjuncts = new ArrayList<>();
                List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), rootNode);
                sbSelectFrom.append(tempSb);
                for (String whereClauseConjunct : whereClauseConjuncts) {
                    sbSelectFrom.append(" AND ").append(whereClauseConjunct);
                }
                sbSelectFrom.append(')');
            } else {
                List<String> whereClauseConjuncts = new ArrayList<>();
                List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null);
                sbSelectFrom.append(tempSb);
                for (String whereClauseConjunct : whereClauseConjuncts) {
                    sbSelectFrom.append(" AND ").append(whereClauseConjunct);
                }
            }

            // Create the select query strings that are used for the set items
            // The idea is to encode a set item as an equality predicate in a dedicated query
            // Each query will then participate in the overall query as parameter container
            // and the extracted equality predicates can be used as-is for the set clause
            cachedBaseQueryStrings = new ArrayList<>();
            StringBuilder sbSetExpressionQuery = new StringBuilder();

            List<SelectInfo> selectInfos = selectManager.getSelectInfos();
            for (Map.Entry<String, Integer> attributeEntry : setAttributeBindingMap.entrySet()) {
                String expression = attributeEntry.getKey();
                int collectionIndex = expression.indexOf(collectionName);
                if (collectionIndex == -1) {
                    expression = entityAlias + '.' + expression;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(expression, 0, collectionIndex);
                    sb.append(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
                    sb.append(expression, collectionIndex + collectionName.length(), expression.length());
                    expression = sb.toString();
                }
                fillCachedBaseQueryStrings(sbSetExpressionQuery, expression, selectInfos.get(attributeEntry.getValue()).getExpression());
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
                .append(' ').append(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
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
        return !trimmedPath.startsWith(JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS) && super.appendSetElementEntityPrefix(trimmedPath);
    }

    @Override
    protected void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }

        JpaUtils.expandBindings(setAttributeBindingMap, collectionColumnBindingMap, collectionAttributeEntries, ClauseType.SET, this, keyFunctionExpression);
        super.prepareAndCheck();
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

        ExtendedQuerySupport extendedQuerySupport = getService(ExtendedQuerySupport.class);
        String sql = extendedQuerySupport.getSql(em, baseQuery);
        String ownerAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, entityAlias);
        String targetAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, JoinManager.COLLECTION_DML_BASE_QUERY_ALIAS);
        JoinTable joinTable = collectionAttribute.getJoinTable();
        int joinTableIndex = SqlUtils.indexOfTableName(sql, joinTable.getTableName());
        String collectionAlias = SqlUtils.extractAlias(sql, joinTableIndex + joinTable.getTableName().length());

        String updateSql = "update " + joinTable.getTableName() + " set ";

        // Prepare a Map<EntityAlias.idColumnName, CollectionAlias.idColumnName>
        // This is used to replace references to id columns properly in the final sql query
        Map<String, String> columnOnlyRemappings = new HashMap<>();
        Map<String, String> columnExpressionRemappings = new HashMap<>();

        String[] discriminatorColumnCheck = mainQuery.jpaProvider.getDiscriminatorColumnCheck(entityType);
        String discriminatorPredicate = "";
        if (discriminatorColumnCheck != null) {
            discriminatorPredicate = ownerAlias + "." + discriminatorColumnCheck[0] + "=" + discriminatorColumnCheck[1] + " and";
            columnExpressionRemappings.put(ownerAlias + "." + discriminatorColumnCheck[0] + "=" + discriminatorColumnCheck[1], "1=1");
        }
        if (joinTable.getKeyColumnMappings() != null) {
            for (Map.Entry<String, String> entry : joinTable.getKeyColumnMappings().entrySet()) {
                columnOnlyRemappings.put(collectionAlias + "." + entry.getValue(), entry.getKey());
                columnExpressionRemappings.put(CollectionDmlSupportFunction.FUNCTION_NAME + "(" + collectionAlias + "." + entry.getValue() + ")", joinTable.getTableName() + "." + entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : joinTable.getIdColumnMappings().entrySet()) {
            columnOnlyRemappings.put(ownerAlias + "." + entry.getValue(), entry.getKey());
            columnExpressionRemappings.put(CollectionDmlSupportFunction.FUNCTION_NAME + "(" + ownerAlias + "." + entry.getValue() + ")", joinTable.getTableName() + "." + entry.getKey());
        }
        for (Map.Entry<String, String> entry : joinTable.getTargetColumnMappings().entrySet()) {
            columnOnlyRemappings.put(targetAlias + "." + entry.getValue(), entry.getKey());
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
