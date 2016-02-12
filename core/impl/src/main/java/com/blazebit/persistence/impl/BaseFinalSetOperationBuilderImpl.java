/*
 * Copyright 2015 Blazebit.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.spi.DbmsModificationState;
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
        super(mainQuery, isMainQuery, DbmsStatementType.SELECT, clazz, null);
        this.endSetResult = endSetResult;
        this.setOperationManager = new SetOperationManager(operator, nested);
        this.orderByElements = new ArrayList<DefaultOrderByElement>(0);
    }
    
    private static boolean isNested(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            return ((BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder).setOperationManager.isNested();
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
    protected void getQueryString1(StringBuilder sbSelectFrom) {
        boolean nested = isNested(setOperationManager.getStartQueryBuilder());
        if (nested) {
            sbSelectFrom.append('(');
        }
        
        setOperationManager.getStartQueryBuilder().getQueryString1(sbSelectFrom);
        
        if (nested) {
            sbSelectFrom.append(')');
        }
        
        if (setOperationManager.hasSetOperations()) {
            String operator = getOperator(setOperationManager.getOperator());
            for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
                sbSelectFrom.append("\n");
                sbSelectFrom.append(operator);
                sbSelectFrom.append("\n");
                
                nested = isNested(setOperand);
                if (nested) {
                    sbSelectFrom.append('(');
                }
                
                setOperand.getQueryString1(sbSelectFrom);
                
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
        TypedQuery<T> leftMostQuery = (TypedQuery<T>) setOperationManager.getStartQueryBuilder().getTypedQuery();
        
        TypedQuery<T> baseQuery;
        String sqlQuery;
        List<Query> participatingQueries = new ArrayList<Query>();
        
        if (leftMostQuery instanceof CustomSQLQuery) {
            CustomSQLQuery customQuery = (CustomSQLQuery) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            participatingQueries.addAll(customQueryParticipants);
            baseQuery = (TypedQuery<T>) customQueryParticipants.get(0);
            sqlQuery = customQuery.getSql();
        } else if (leftMostQuery instanceof CustomSQLTypedQuery<?>) {
            CustomSQLTypedQuery<?> customQuery = (CustomSQLTypedQuery<?>) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            participatingQueries.addAll(customQueryParticipants);
            baseQuery = (TypedQuery<T>) customQueryParticipants.get(0);
            sqlQuery = customQuery.getSql();
        } else {
            baseQuery = leftMostQuery;
            participatingQueries.add(baseQuery);
            sqlQuery = cbf.getExtendedQuerySupport().getSql(em, baseQuery);
        }
        
        int size = sqlQuery.length() + 10;
        List<String> setOperands = new ArrayList<String>();
        setOperands.add(sqlQuery);
        
        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
            Query q = setOperand.getQuery();
            String setOperandSql;
            
            if (q instanceof CustomSQLQuery) {
                CustomSQLQuery customQuery = (CustomSQLQuery) q;
                List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
                participatingQueries.addAll(customQueryParticipants);
                
                setOperandSql = customQuery.getSql();
            } else if (q instanceof CustomSQLTypedQuery<?>) {
                CustomSQLTypedQuery<?> customQuery = (CustomSQLTypedQuery<?>) q;
                List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
                participatingQueries.addAll(customQueryParticipants);

                setOperandSql = customQuery.getSql();
            } else {
                setOperandSql = cbf.getExtendedQuerySupport().getSql(em, q);
                participatingQueries.add(q);
            }
            
            setOperands.add(setOperandSql);
            size += setOperandSql.length() + 30;
        }

        StringBuilder sqlSb = new StringBuilder(size);
        
        String limit = null;
        String offset = null;
        
        if (firstResult != 0) {
            offset = Integer.toString(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
            limit = Integer.toString(maxResults);
        }

        dbmsDialect.appendSet(sqlSb, setOperationManager.getOperator(), setOperationManager.isNested(), setOperands, getOrderByElements(), limit, offset);
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, false, participatingQueries);
        applyExtendedSql(sqlSb, false, false, withClause, null, null);
        
        String finalQuery = sqlSb.toString();
        TypedQuery<T> query = new CustomSQLTypedQuery<T>(participatingQueries, baseQuery, cbf, dbmsDialect, em, cbf.getExtendedQuerySupport(), finalQuery);

        // TODO: needs tests
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }
        
        return query;
    }
    
    protected Map<String, String> applyExtendedSql(StringBuilder sqlSb, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        // No limit/offset here because we need to handle that differently 
        return dbmsDialect.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, null, null, returningColumns, includedModificationStates);
    }

    protected String getOperator(SetOperationType type) {
        switch (type) {
            case UNION: return "UNION";
            case UNION_ALL: return "UNION ALL";
            case INTERSECT: return "INTERSECT";
            case INTERSECT_ALL: return "INTERSECT ALL";
            case EXCEPT: return "EXCEPT";
            case EXCEPT_ALL: return "EXCEPT ALL";
        }
        
        return null;
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
