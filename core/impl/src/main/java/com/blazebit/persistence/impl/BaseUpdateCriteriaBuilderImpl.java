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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.UpdateModificationQuerySpecification;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.spi.AttributePath;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.UpdateJoinStyle;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseUpdateCriteriaBuilderImpl<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseUpdateCriteriaBuilder<T, X>, SubqueryBuilderListener<X>, ExpressionBuilderEndedListener {

    protected final Map<String, Integer> setAttributeBindingMap = new LinkedHashMap<>();
    private SubqueryInternalBuilder<X> currentSubqueryBuilder;
    private String currentAttribute;

    public BaseUpdateCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.UPDATE, clazz, alias, cteKey, cteClass, result, listener);
    }

    public BaseUpdateCriteriaBuilderImpl(BaseUpdateCriteriaBuilderImpl<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
        builder.verifyBuilderEnded();
        for (Entry<String, Integer> entry : builder.setAttributeBindingMap.entrySet()) {
            this.setAttributeBindingMap.put(entityType.getName(), entry.getValue());
        }
    }

    @Override
    protected void collectParameters() {
        ParameterRegistrationVisitor parameterRegistrationVisitor = parameterManager.getParameterRegistrationVisitor();
        ClauseType oldClauseType = parameterRegistrationVisitor.getClauseType();
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterRegistrationVisitor.getQueryBuilder();
        try {
            parameterRegistrationVisitor.setQueryBuilder(this);
            parameterRegistrationVisitor.setClauseType(ClauseType.SET);
            selectManager.acceptVisitor(parameterRegistrationVisitor);
            parameterRegistrationVisitor.setClauseType(ClauseType.WHERE);
            whereManager.acceptVisitor(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(oldClauseType);
            parameterRegistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public X set(String attributeName, Object value) {
        verifyBuilderEnded();
        addAttribute(attributeName);
        Expression attributeExpression = parameterManager.addParameterExpression(value, ClauseType.SET, this);
        selectManager.select(attributeExpression, null);
        return (X) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X setNull(String attributeName) {
        return setExpression(attributeName, "NULL");
    }

    @Override
    @SuppressWarnings("unchecked")
    public X setExpression(String attributeName, String expression) {
        verifyBuilderEnded();
        addAttribute(attributeName);
        Expression attributeExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        selectManager.select(attributeExpression, null);
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SubqueryInitiator<X> set(String attribute) {
        verifySubqueryBuilderEnded();
        addAttribute(attribute);
        this.currentAttribute = attribute;
        return subqueryInitFactory.createSubqueryInitiator((X) this, this, false, ClauseType.SET);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleSubqueryInitiator<X> setSubqueries(String attribute, String expression) {
        verifySubqueryBuilderEnded();
        addAttribute(attribute);
        this.currentAttribute = attribute;
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>((X) this, expr, this, subqueryInitFactory, ClauseType.SET);
        return initiator;
    }

    @Override
    public SubqueryBuilder<X> set(String attribute, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        addAttribute(attribute);
        this.currentAttribute = attribute;
        return subqueryInitFactory.createSubqueryBuilder((X) this, this, false, criteriaBuilder, ClauseType.SET);
    }

    private void verifySubqueryBuilderEnded() {
        if (currentAttribute != null) {
            throw new BuilderChainingException("An initiator was not ended properly.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("An subquery builder was not ended properly.");
        }
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }

        Expression attributeExpression = builder.getExpression();
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        selectManager.select(attributeExpression, null);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<X> builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        if (currentSubqueryBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        Expression attributeExpression = new SubqueryExpression(builder);
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        selectManager.select(attributeExpression, null);
        currentAttribute = null;
        currentSubqueryBuilder = null;
    }

    @Override
    public void onBuilderStarted(SubqueryInternalBuilder<X> builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to start a builder without an originating initiator.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }
        currentSubqueryBuilder = builder;
    }

    @Override
    public void onReplaceBuilder(SubqueryInternalBuilder<X> oldBuilder, SubqueryInternalBuilder<X> newBuilder) {
        throw new IllegalArgumentException("Replace not valid!");
    }

    @Override
    public void onInitiatorStarted(SubqueryInitiator<?> initiator) {
        throw new IllegalArgumentException("Initiator started not valid!");
    }

    protected void addAttribute(String attributeName) {
        // Just do that to assert the attribute exists
        JpaMetamodelAccessor jpaMetamodelAccessor = mainQuery.jpaProvider.getJpaMetamodelAccessor();
        jpaMetamodelAccessor.getBasicAttributePath(getMetamodel(), entityType, attributeName);
        Integer attributeBindIndex = setAttributeBindingMap.get(attributeName);

        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }

        setAttributeBindingMap.put(attributeName, selectManager.getSelectInfos().size());
    }

    @Override
    protected void prepareSelect() {
        JoinNode rootNode = joinManager.getRoots().get(0);
        // We only need this when rendering a plain update statement, but not when doing SQL replacement
        boolean enableElementCollectionIdCutoff = (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) && mainQuery.dbmsDialect.getUpdateJoinStyle() != UpdateJoinStyle.NONE;
        // We have an update statement here which supports parameters in SELECT/SET clause
        JpaUtils.expandBindings(setAttributeBindingMap, null, mainQuery.metamodel.getManagedType(ExtendedManagedType.class, entityType).getOwnedSingularAttributes(), ClauseType.SET, this, null, enableElementCollectionIdCutoff);
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode, boolean countWrapped) {
        sbSelectFrom.append("UPDATE ");
        sbSelectFrom.append(entityType.getName()).append(' ');
        sbSelectFrom.append(entityAlias);
        appendSetClause(sbSelectFrom, externalRepresentation);
        JoinNode rootNode = joinManager.getRoots().get(0);
        if (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) {
            if (externalRepresentation) {
                List<String> whereClauseConjuncts = new ArrayList<>();
                List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, true);
                appendWhereClause(sbSelectFrom, externalRepresentation);
            } else {
                boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
                queryGenerator.setExternalRepresentation(externalRepresentation);
                try {
                    if (mainQuery.dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.NONE) {
                        sbSelectFrom.append(" WHERE EXISTS (SELECT 1");
                        List<String> whereClauseConjuncts = new ArrayList<>();
                        List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                        joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), rootNode, true);
                        appendWhereClause(sbSelectFrom, externalRepresentation);
                        sbSelectFrom.append(')');
                    } else {
                        sbSelectFrom.setLength(0);
                        if (mainQuery.dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.MERGE || mainQuery.dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.REFERENCE) {
                            // We have to build a query that puts the set clause expressions into the group by to align with the parameter positions in the final SQL
                            sbSelectFrom.append("SELECT 1");
                            List<String> whereClauseConjuncts = new ArrayList<>();
                            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                            joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, true);
                            appendWhereClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts, lateralJoinNode);
                            sbSelectFrom.append(" GROUP BY ");
                            appendSetElementsAsCaseExpressions(sbSelectFrom);
                        } else {
                            sbSelectFrom.append("SELECT ");
                            appendSetElementsAsCaseExpressions(sbSelectFrom);

                            List<String> whereClauseConjuncts = new ArrayList<>();
                            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                            joinManager.buildClause(sbSelectFrom, EnumSet.noneOf(ClauseType.class), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, true);

                            appendWhereClause(sbSelectFrom, whereClauseConjuncts, optionalWhereClauseConjuncts, lateralJoinNode);
                        }
                    }
                } finally {
                    queryGenerator.setExternalRepresentation(originalExternalRepresentation);
                }
            }
        } else {
            appendWhereClause(sbSelectFrom, externalRepresentation);
        }
    }

    protected void appendSetElementsAsCaseExpressions(StringBuilder sbSelectFrom) {
        queryGenerator.setClauseType(ClauseType.SET);
        queryGenerator.setQueryBuffer(sbSelectFrom);
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        queryGenerator.setExternalRepresentation(false);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        try {
            List<SelectInfo> selectInfos = selectManager.getSelectInfos();
            Iterator<Entry<String, Integer>> setAttributeIter = setAttributeBindingMap.entrySet().iterator();
            if (setAttributeIter.hasNext()) {
                Map.Entry<String, Integer> attributeEntry = setAttributeIter.next();
                sbSelectFrom.append("CASE WHEN ");
                appendSetElementAsSelectItem(sbSelectFrom, attributeEntry.getKey());
                sbSelectFrom.append(" = ");
                queryGenerator.generate(selectInfos.get(attributeEntry.getValue()).getExpression());
                sbSelectFrom.append(" THEN 1 ELSE 0 END");
                while (setAttributeIter.hasNext()) {
                    attributeEntry = setAttributeIter.next();
                    sbSelectFrom.append(", ");
                    sbSelectFrom.append("CASE WHEN ");
                    appendSetElementAsSelectItem(sbSelectFrom, attributeEntry.getKey());
                    sbSelectFrom.append(" = ");
                    queryGenerator.generate(selectInfos.get(attributeEntry.getValue()).getExpression());
                    sbSelectFrom.append(" THEN 1 ELSE 0 END");
                }
            }
        } finally {
            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
            queryGenerator.setClauseType(null);
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
        }
    }

    @Override
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        prepareAndCheck();
        JoinNode rootNode = joinManager.getRoots().get(0);
        if (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) {
            // Prefer an exists subquery instead of MERGE
            if (mainQuery.dbmsDialect.getUpdateJoinStyle() == UpdateJoinStyle.NONE) {
                return super.getQuery(includedModificationStates);
            }
            Query baseQuery = em.createQuery(getBaseQueryStringWithCheck(null, null));
            QuerySpecification querySpecification = getQuerySpecification(baseQuery, getCountExampleQuery(), getReturningColumns(), null, includedModificationStates);

            CustomSQLQuery query = new CustomSQLQuery(
                    querySpecification,
                    baseQuery,
                    parameterManager.getCriteriaNameMapping(),
                    parameterManager.getTransformers(),
                    parameterManager.getValuesParameters(),
                    parameterManager.getValuesBinders()
            );

            parameterManager.parameterizeQuery(query);

            return query;
        } else {
            return super.getQuery(includedModificationStates);
        }
    }

    @Override
    protected <R> TypedQuery<ReturningResult<R>> getExecuteWithReturningQuery(TypedQuery<Object[]> exampleQuery, Query baseQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder) {
        QuerySpecification querySpecification = getQuerySpecification(baseQuery, exampleQuery, returningColumns, objectBuilder, null);

        CustomReturningSQLTypedQuery<R> query = new CustomReturningSQLTypedQuery<R>(
                querySpecification,
                exampleQuery,
                parameterManager.getCriteriaNameMapping(),
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        parameterManager.parameterizeQuery(query);
        return query;
    }

    private <R> QuerySpecification getQuerySpecification(Query baseQuery, Query exampleQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder, Map<DbmsModificationState, String> includedModificationStates) {
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);

        boolean isEmbedded = this instanceof ReturningBuilder;
        boolean shouldRenderCteNodes = renderCteNodes(isEmbedded);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(isEmbedded) : Collections.EMPTY_LIST;

        List<String> setColumns = getSetColumns();
        ExtendedQuerySupport extendedQuerySupport = getService(ExtendedQuerySupport.class);
        Map<String, String> aliasMapping = new TreeMap<>();
        JoinNode rootNode = joinManager.getRoots().get(0);
        String[] idColumns = null;
        String tableToUpdate = null;
        String tableAlias = null;
        if ((joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) && mainQuery.dbmsDialect.getUpdateJoinStyle() != UpdateJoinStyle.NONE) {
            String sql = getService(ExtendedQuerySupport.class).getSql(em, baseQuery);
            if (SqlUtils.indexOfSelect(sql) != -1) {
                idColumns = getIdColumns(getMetamodel().getManagedType(ExtendedManagedType.class, entityType));
                int fromIndex = SqlUtils.indexOfFrom(sql);
                int tableStartIndex = fromIndex + SqlUtils.FROM.length();
                int tableEndIndex = sql.indexOf(" ", tableStartIndex);
                tableToUpdate = sql.substring(tableStartIndex, tableEndIndex);
                tableAlias = extendedQuerySupport.getSqlAlias(em, baseQuery, entityAlias);

                for (String idColumn : idColumns) {
                    aliasMapping.put(tableAlias + "." + idColumn, "tmp.c" + aliasMapping.size());
                }
                SqlUtils.buildAliasMappingForTopLevelSelects(extendedQuerySupport.getSql(em, baseQuery), "tmp", aliasMapping);
            }
        }

        return new UpdateModificationQuerySpecification(
                this,
                baseQuery,
                exampleQuery,
                parameterManager.getParameterImpls(),
                parameterListNames,
                mainQuery.cteManager.isRecursive(),
                ctes,
                shouldRenderCteNodes,
                isEmbedded,
                returningColumns,
                objectBuilder,
                includedModificationStates,
                returningAttributeBindingMap,
                mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled(),
                tableToUpdate,
                tableAlias,
                idColumns,
                setColumns,
                getForeignKeyParticipatingQueries(),
                aliasMapping,
                getUpdateExampleQuery()
        );
    }

    protected List<String> getSetColumns() {
        StringBuilder setExtractionSb = new StringBuilder();
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        StringBuilder originalQueryBuffer = queryGenerator.getQueryBuffer();
        queryGenerator.setExternalRepresentation(false);
        queryGenerator.setQueryBuffer(setExtractionSb);
        try {
            setExtractionSb.append("SELECT ");
            Iterator<Entry<String, Integer>> setAttributeIter = setAttributeBindingMap.entrySet().iterator();
            if (setAttributeIter.hasNext()) {
                Map.Entry<String, Integer> attributeEntry = setAttributeIter.next();
                appendSetElementAsSelectItem(setExtractionSb, attributeEntry.getKey());
                while (setAttributeIter.hasNext()) {
                    setExtractionSb.append(", ");
                    appendSetElementAsSelectItem(setExtractionSb, setAttributeIter.next().getKey());
                }
            }

            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            joinManager.buildClause(setExtractionSb, EnumSet.noneOf(ClauseType.class), null, false, false, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, true);

            appendWhereClause(setExtractionSb, whereClauseConjuncts, optionalWhereClauseConjuncts, null);
        } finally {
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
            queryGenerator.setQueryBuffer(originalQueryBuffer);
        }

        String sql = getService(ExtendedQuerySupport.class).getSql(em, em.createQuery(setExtractionSb.toString()));
        return Arrays.asList(SqlUtils.getSelectItems(sql, 0, new SqlUtils.SelectItemExtractor() {
            @Override
            public String extract(StringBuilder sb, int index, int currentPosition) {
                return sb.substring(sb.indexOf(".") + 1, sb.indexOf(" ")).trim();
            }
        }));
    }

    protected Collection<Query> getForeignKeyParticipatingQueries() {
        Map<String, Query> map = null;
        JpaMetamodelAccessor jpaMetamodelAccessor = mainQuery.jpaProvider.getJpaMetamodelAccessor();
        for (String attributeName : setAttributeBindingMap.keySet()) {
            AttributePath path = jpaMetamodelAccessor.getBasicAttributePath(getMetamodel(), entityType, attributeName);
            for (Attribute<?, ?> attributePart : path.getAttributes()) {
                if (attributePart instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attributePart;
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    if (singularAttribute.getType() instanceof EntityType<?>) {
                        String entityName = ((EntityType<?>) singularAttribute.getType()).getName();
                        if (!map.containsKey(entityName)) {
                            map.put(entityName, em.createQuery("select e from " + entityName + " e"));
                        }
                        break;
                    }
                }
            }
        }
        return map == null ? Collections.<Query>emptyList() : map.values();
    }

    protected void appendSetClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        sbSelectFrom.append(" SET ");

        queryGenerator.setClauseType(ClauseType.SET);
        queryGenerator.setQueryBuffer(sbSelectFrom);
        boolean originalExternalRepresentation = queryGenerator.isExternalRepresentation();
        queryGenerator.setExternalRepresentation(externalRepresentation);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        try {
            List<SelectInfo> selectInfos = selectManager.getSelectInfos();
            Iterator<Entry<String, Integer>> setAttributeIter = setAttributeBindingMap.entrySet().iterator();
            if (setAttributeIter.hasNext()) {
                Map.Entry<String, Integer> attributeEntry = setAttributeIter.next();
                appendSetElement(sbSelectFrom, attributeEntry.getKey(), selectInfos.get(attributeEntry.getValue()).getExpression());
                while (setAttributeIter.hasNext()) {
                    attributeEntry = setAttributeIter.next();
                    sbSelectFrom.append(", ");
                    appendSetElement(sbSelectFrom, attributeEntry.getKey(), selectInfos.get(attributeEntry.getValue()).getExpression());
                }
            }
        } finally {
            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
            queryGenerator.setClauseType(null);
            queryGenerator.setExternalRepresentation(originalExternalRepresentation);
        }
    }

    protected final void appendSetElement(StringBuilder sbSelectFrom, String attribute, Expression valueExpression) {
        String trimmedPath = attribute.trim();
        if (appendSetElementEntityPrefix(trimmedPath)) {
            sbSelectFrom.append(entityAlias).append('.');
        }
        sbSelectFrom.append(attribute);
        sbSelectFrom.append(" = ");
        queryGenerator.generate(valueExpression);
    }

    protected void appendSetElementAsSelectItem(StringBuilder sbSelectFrom, String attribute) {
        String trimmedPath = attribute.trim();
        if (appendSetElementEntityPrefix(trimmedPath)) {
            sbSelectFrom.append(entityAlias).append('.');
        }
        sbSelectFrom.append(attribute);
    }

    protected boolean appendSetElementEntityPrefix(String trimmedPath) {
        String indexStart = "index(";
        String keyStart = "key(";
        return !trimmedPath.regionMatches(true, 0, indexStart, 0, indexStart.length())
                && !trimmedPath.regionMatches(true, 0, keyStart, 0, keyStart.length());
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
