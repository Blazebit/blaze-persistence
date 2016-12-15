/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.*;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.SetOperationQuerySpecification;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class BaseFinalSetOperationBuilderImpl<T, X extends BaseFinalSetOperationBuilder<T, X>, Y extends BaseFinalSetOperationBuilderImpl<T, X, Y>> extends AbstractCommonQueryBuilder<T, X, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, Y> implements BaseFinalSetOperationBuilder<T, X>, BaseOngoingFinalSetOperationBuilder<T, X> {

    protected T endSetResult;
    
    protected final SetOperationManager setOperationManager;
    protected final List<DefaultOrderByElement> orderByElements;

    public BaseFinalSetOperationBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, T endSetResult) {
        super(mainQuery, isMainQuery, DbmsStatementType.SELECT, clazz, null, null, false);
        this.endSetResult = endSetResult;
        this.setOperationManager = new SetOperationManager(operator, nested);
        this.orderByElements = new ArrayList<DefaultOrderByElement>(0);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && setOperationManager.isEmpty();
    }
    
    private static boolean isNestedAndComplex(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            BaseFinalSetOperationBuilderImpl<?, ?, ?> builder = (BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder;
            return builder.setOperationManager.isNested() && (builder.setOperationManager.hasSetOperations() || isNestedAndComplex(builder.setOperationManager.getStartQueryBuilder()));
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public X orderBy(String expression, boolean ascending, boolean nullFirst) {
        prepareAndCheck();
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> leftMostQuery = getLeftMost(setOperationManager.getStartQueryBuilder());
        
        AliasInfo aliasInfo = leftMostQuery.aliasManager.getAliasInfo(expression);
        if (aliasInfo != null) {
            // find out the position by JPQL alias
            int position = cbf.getExtendedQuerySupport().getSqlSelectAliasPosition(em, leftMostQuery.getTypedQuery(), expression);
            orderByElements.add(new DefaultOrderByElement(expression, position, ascending, nullFirst));
            return (X) this;
        }

        int position = cbf.getExtendedQuerySupport().getSqlSelectAttributePosition(em, leftMostQuery.getTypedQuery(), expression);
        orderByElements.add(new DefaultOrderByElement(expression, position, ascending, nullFirst));
        
        return (X) this;
    }
    
    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getLeftMost(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            return getLeftMost(((BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder).setOperationManager.getStartQueryBuilder());
        }
        
        return queryBuilder;
    }
    
    protected List<? extends OrderByElement> getOrderByElements() {
        return orderByElements;
    }
    
    public T getEndSetResult() {
        return endSetResult;
    }
    
    public void setEndSetResult(T endSetResult) {
        this.endSetResult = endSetResult;
    }

    public T endSet() {
        return endSetResult;
    }

    @Override
    protected void prepareAndCheck() {
        // nothing to do here
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        boolean nested = isNestedAndComplex(setOperationManager.getStartQueryBuilder());
        if (nested) {
            sbSelectFrom.append('(');
        }
        
        setOperationManager.getStartQueryBuilder().buildBaseQueryString(sbSelectFrom, externalRepresentation);
        
        if (nested) {
            sbSelectFrom.append(')');
        }
        
        if (setOperationManager.hasSetOperations()) {
            String operator = getOperator(setOperationManager.getOperator());
            for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
                sbSelectFrom.append("\n");
                sbSelectFrom.append(operator);
                sbSelectFrom.append("\n");
                
                nested = isNestedAndComplex(setOperand);
                if (nested) {
                    sbSelectFrom.append('(');
                }
                
                setOperand.buildBaseQueryString(sbSelectFrom, externalRepresentation);
                
                if (nested) {
                    sbSelectFrom.append(')');
                }
            }
            
            applySetOrderBy(sbSelectFrom);
            applyJpaLimit(sbSelectFrom);
        }
    }
    
    protected void applySetOrderBy(StringBuilder sbSelectFrom) {
        if (orderByElements.isEmpty()) {
            return;
        }
        
        sbSelectFrom.append("\nORDER BY ");
        
        for (int i = 0; i < orderByElements.size(); i++) {
            if (i != 0) {
                sbSelectFrom.append(", ");
            }
            
            DefaultOrderByElement elem = orderByElements.get(i);
            sbSelectFrom.append(elem.getName());

            if (elem.isAscending()) {
                sbSelectFrom.append(" ASC");
            } else {
                sbSelectFrom.append(" DESC");
            }
            
            if (elem.isNullsFirst()) {
                sbSelectFrom.append(" NULLS FIRST");
            } else {
                sbSelectFrom.append(" NULLS LAST");
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TypedQuery<T> getTypedQuery() {
        Set<String> parameterListNames = new HashSet<String>();
        TypedQuery<T> leftMostQuery = (TypedQuery<T>) setOperationManager.getStartQueryBuilder().getTypedQuery();
        TypedQuery<T> baseQuery;

        parameterManager.collectParameterListNames(leftMostQuery, parameterListNames);

        if (leftMostQuery instanceof CustomSQLQuery) {
            CustomSQLQuery customQuery = (CustomSQLQuery) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            baseQuery = (TypedQuery<T>) customQueryParticipants.get(0);
        } else if (leftMostQuery instanceof CustomSQLTypedQuery<?>) {
            CustomSQLTypedQuery<?> customQuery = (CustomSQLTypedQuery<?>) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            baseQuery = (TypedQuery<T>) customQueryParticipants.get(0);
        } else {
            baseQuery = leftMostQuery;
        }
        
        List<Query> setOperands = new ArrayList<Query>();

        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
            Query q = setOperand.getQuery();
            setOperands.add(q);
            parameterManager.collectParameterListNames(q, parameterListNames);
        }

        String limit = null;
        String offset = null;
        
        if (firstResult != 0) {
            offset = Integer.toString(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            limit = Integer.toString(maxResults);
        }

        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, Collections.EMPTY_SET);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new SetOperationQuerySpecification(
                this,
                leftMostQuery,
                baseQuery,
                setOperands,
                setOperationManager.getOperator(),
                getOrderByElements(),
                setOperationManager.isNested(),
                parameterListNames,
                limit,
                offset,
                keyRestrictedLeftJoinAliases,
                entityFunctionNodes,
                mainQuery.cteManager.isRecursive(),
                ctes,
                shouldRenderCteNodes
        );
        
        // Unfortunately we need this little adapter here
        @SuppressWarnings("rawtypes")
        TypedQuery<T> query = new CustomSQLTypedQuery<T>(
                querySpecification,
                baseQuery,
                new CommonQueryBuilderAdapter(this),
                cbf.getExtendedQuerySupport(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        // TODO: needs tests
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }
        
        return query;
    }
    
    protected String getOperator(SetOperationType type) {
        switch (type) {
            case UNION: return "UNION";
            case UNION_ALL: return "UNION ALL";
            case INTERSECT: return "INTERSECT";
            case INTERSECT_ALL: return "INTERSECT ALL";
            case EXCEPT: return "EXCEPT";
            case EXCEPT_ALL: return "EXCEPT ALL";
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    @Override
    public TypedQuery<T> getQuery() {
        return getTypedQuery();
    }

    public List<T> getResultList() {
        return getTypedQuery().getResultList();
    }

    public T getSingleResult() {
        return getTypedQuery().getSingleResult();
    }

}
