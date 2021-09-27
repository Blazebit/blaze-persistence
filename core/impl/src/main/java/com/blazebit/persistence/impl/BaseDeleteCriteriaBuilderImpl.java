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

import com.blazebit.persistence.BaseDeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.DeleteModificationQuerySpecification;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseDeleteCriteriaBuilderImpl<T, X extends BaseDeleteCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseDeleteCriteriaBuilder<T, X> {

    public BaseDeleteCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.DELETE, clazz, alias, cteKey, cteClass, result, listener);
    }

    public BaseDeleteCriteriaBuilderImpl(AbstractModificationCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode, boolean countWrapped) {
        sbSelectFrom.append("DELETE FROM ");
        sbSelectFrom.append(entityType.getName()).append(' ');
        sbSelectFrom.append(entityAlias);
        JoinNode rootNode = joinManager.getRoots().get(0);
        if (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) {
            if (externalRepresentation) {
                sbSelectFrom.append(" USING ");
                List<String> whereClauseConjuncts = new ArrayList<>();
                List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, false);
                appendWhereClause(sbSelectFrom, externalRepresentation);
            } else {
                if (!mainQuery.supportsAdvancedSql() || mainQuery.dbmsDialect.getDeleteJoinStyle() == DeleteJoinStyle.NONE || mainQuery.dbmsDialect.getDeleteJoinStyle() == DeleteJoinStyle.MERGE) {
                    sbSelectFrom.append(" WHERE EXISTS (SELECT 1");
                    List<String> whereClauseConjuncts = new ArrayList<>();
                    List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                    joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), rootNode, true);
                    appendWhereClause(sbSelectFrom, externalRepresentation);
                    sbSelectFrom.append(')');
                } else {
                    sbSelectFrom.setLength(0);
                    sbSelectFrom.append("SELECT 1");
                    List<String> whereClauseConjuncts = new ArrayList<>();
                    List<String> optionalWhereClauseConjuncts = new ArrayList<>();
                    joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), null, true);
                    appendWhereClause(sbSelectFrom, externalRepresentation);
                }
            }
        } else {
            appendWhereClause(sbSelectFrom, externalRepresentation);
        }
    }

    @Override
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        prepareAndCheck();
        JoinNode rootNode = joinManager.getRoots().get(0);
        if (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) {
            // Prefer an exists subquery instead of MERGE
            if (!mainQuery.supportsAdvancedSql() || mainQuery.dbmsDialect.getDeleteJoinStyle() == DeleteJoinStyle.NONE || mainQuery.dbmsDialect.getDeleteJoinStyle() == DeleteJoinStyle.MERGE) {
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

        String sql = getService(ExtendedQuerySupport.class).getSql(em, baseQuery);
        String[] idColumns = null;
        String tableToDelete = null;
        String tableAlias = null;
        boolean innerJoinOnly = false;
        if (SqlUtils.indexOfSelect(sql) != -1) {
            idColumns = getIdColumns(getMetamodel().getManagedType(ExtendedManagedType.class, entityType));
            innerJoinOnly = joinManager.getRoots().size() == 1 && !joinManager.getRootNodeOrFail(null).accept(InnerJoinOnlyAbortableResultJoinNodeVisitor.INSTANCE);
            int fromIndex = SqlUtils.indexOfFrom(sql);
            int tableStartIndex = fromIndex + SqlUtils.FROM.length();
            int tableEndIndex = sql.indexOf(" ", tableStartIndex);
            int tableAliasEndIndex = sql.indexOf(" ", tableEndIndex + 1);
            tableToDelete = sql.substring(tableStartIndex, tableEndIndex);
            tableAlias = sql.substring(tableEndIndex + 1, tableAliasEndIndex);
        }

        return new DeleteModificationQuerySpecification(
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
                tableToDelete,
                tableAlias,
                idColumns,
                innerJoinOnly,
                getDeleteExampleQuery()
        );
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
