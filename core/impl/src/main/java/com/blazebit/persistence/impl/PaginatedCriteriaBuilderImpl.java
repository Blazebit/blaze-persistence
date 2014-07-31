/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @param <T> The query result type
 * @author Moritz Becker
 * @author Christian Beikov
 */
public class PaginatedCriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, PaginatedCriteriaBuilder<T>> implements
    PaginatedCriteriaBuilder<T> {

    private final int firstRow;
    private final int pageSize;

    public PaginatedCriteriaBuilderImpl(AbstractQueryBuilder<T, ? extends QueryBuilder<T, ?>> baseBuilder, int firstRow, int pageSize) {
        super(baseBuilder);
        if (firstRow < 0) {
            throw new IllegalArgumentException("firstRow may not be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize may not be zero or negative");
        }
        this.firstRow = firstRow;
        this.pageSize = pageSize;
    }

    @Override
    public PagedList<T> getResultList() {
        String countQueryString = getPageCountQueryString();
        TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
        parameterizeQuery(countQuery);

        long totalSize = countQuery.getSingleResult();
        
        if (totalSize == 0L) {
            return new PagedListImpl<T>(totalSize);
        }
        
        String idQueryString = getPageIdQueryString();
        Query idQuery = em.createQuery(idQueryString);
        parameterizeQuery(idQuery);

        List ids = idQuery.setFirstResult((int) firstRow)
            .setMaxResults((int) pageSize)
            .getResultList();
        
        if (ids.isEmpty()) {
            return new PagedListImpl<T>(totalSize);
        }
        
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();
        
        String idClause = new StringBuilder(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .toString();
        
        if (orderByManager.hasNonIdOrderBys(idClause)) {
            // If we have order bys, 
            List newIds = new ArrayList(ids.size());
            
            for (int i = 0; i < ids.size(); i++) {
                newIds.add(((Object[]) ids.get(i))[0]);
            }
            
            ids = newIds;
        }
        
        parameterManager.addParameterMapping(idParamName, ids);

        PagedList<T> pagedResultList = new PagedListImpl<T>(super.getResultList(), totalSize);
        return pagedResultList;
    }

    @Override
    public String getPageCountQueryString() {
        verifyBuilderEnded();
        StringBuilder sbSelectFrom = new StringBuilder();

        applyImplicitJoins();
        applyExpressionTransformers(Arrays.asList(new ExpressionTransformer[]{ new OuterFunctionTransformer(joinManager), new ArrayExpressionTransformer(joinManager) }));

        sbSelectFrom.append("SELECT COUNT(*)");
        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());
        
        StringBuilder sbRemaining = new StringBuilder();
        whereManager.buildClause(sbRemaining);
        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);
        
        StringBuilder sbJoin = new StringBuilder();
        joinManager.buildJoins(false, sbJoin);
        
        return sbSelectFrom.append(sbJoin).append(sbRemaining).toString();
    }

    @Override
    public String getQueryString() {
        verifyBuilderEnded();
        StringBuilder sbSelectFrom = new StringBuilder();
        applyImplicitJoins();
        applyExpressionTransformers(Arrays.asList(new ExpressionTransformer[]{ new OuterFunctionTransformer(joinManager), new ArrayExpressionTransformer(joinManager) }));

        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();

        sbSelectFrom.append(selectManager.buildSelect());
        if (sbSelectFrom.length() > 0) {
            sbSelectFrom.append(' ');
        }
        sbSelectFrom.append("FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());

        StringBuilder sbRemaining = new StringBuilder();
        sbRemaining.append(" WHERE ")
            .append(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .append(" IN (:")
            .append(idParamName)
            .append(")");

        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);
        orderByManager.buildOrderBy(sbRemaining);
        
        StringBuilder sbJoin = new StringBuilder();
        joinManager.buildJoins(true, sbJoin);

        return sbSelectFrom.append(sbJoin).append(sbRemaining).toString();
    }

    @Override
    public String getPageIdQueryString() {
        verifyBuilderEnded();
        StringBuilder sbSelectFrom = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();

        applyImplicitJoins();
        applyExpressionTransformers(Arrays.asList(new ExpressionTransformer[]{ new OuterFunctionTransformer(joinManager), new ArrayExpressionTransformer(joinManager) }));
        
        String idClause = new StringBuilder(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .toString();

        sbSelectFrom.append("SELECT DISTINCT ")
            .append(idClause);
        
        if (orderByManager.hasNonIdOrderBys(idClause)) {
            sbSelectFrom.append(", ");
            orderByManager.buildSelectClauses(sbSelectFrom);
        }
        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());
        
        StringBuilder sbRemaining = new StringBuilder();
        whereManager.buildClause(sbRemaining);
        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);
        orderByManager.buildOrderBy(sbRemaining);
        
        StringBuilder sbJoin = new StringBuilder();
        joinManager.buildJoins(false, sbJoin);

        return sbSelectFrom.append(sbJoin).append(sbRemaining).toString();
    }

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> select(Class<Y> clazz) {
        return (PaginatedCriteriaBuilder<Y>) super.select(clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<PaginatedCriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (PaginatedCriteriaBuilder<Y>) super.selectNew(builder);
    }

    @Override
    public PaginatedCriteriaBuilder<Tuple> select(String expression) {
        return (PaginatedCriteriaBuilder<Tuple>) super.select(expression);
    }

    @Override
    public PaginatedCriteriaBuilder<Tuple> select(String expression, String alias) {
        return (PaginatedCriteriaBuilder<Tuple>) super.select(expression, alias);
    }
}
