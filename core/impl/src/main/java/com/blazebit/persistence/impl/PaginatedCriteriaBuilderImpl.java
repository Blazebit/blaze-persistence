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

import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.KeySet;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.objectbuilder.DelegatingKeySetExtractionObjectBuilder;
import com.blazebit.persistence.impl.objectbuilder.KeySetExtractionObjectBuilder;
import com.blazebit.persistence.spi.QueryTransformer;
import java.io.Serializable;
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
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class PaginatedCriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, PaginatedCriteriaBuilder<T>> implements PaginatedCriteriaBuilder<T> {

    private static final String KEY_SET_PARAMETER_NAME = "_keySetParameter";

    private final boolean extractKeySet;
    private final KeySetImpl keySet;
    
    // Mutable state
    private boolean needsCheck = true;
    
    private int firstRow;
    private int pageSize;
    private boolean needsNewIdList;
    private KeySetMode keySetMode;
    private List<OrderByExpression> orderByExpressions;
    
    // Cache
    private String cachedCountQueryString;
    private String cachedIdQueryString;
    private String cachedQueryString;

    public PaginatedCriteriaBuilderImpl(AbstractQueryBuilder<T, ? extends QueryBuilder<T, ?>> baseBuilder, boolean extractKeySet, KeySetImpl keySet, int firstRow, int pageSize) {
        super(baseBuilder);
        if (firstRow < 0) {
            throw new IllegalArgumentException("firstRow may not be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize may not be zero or negative");
        }
        this.extractKeySet = extractKeySet;
        this.keySet = keySet;
        this.firstRow = firstRow;
        this.pageSize = pageSize;
    }
    
    @Override
    public PagedList<T> getResultList() {
        prepareAndCheck();
        
        String countQueryString = getPageCountQueryString0();
        TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
        parameterizeQuery(countQuery);

        long totalSize = countQuery.getSingleResult();

        if (totalSize == 0L) {
            return new PagedListImpl<T>(null, totalSize);
        }

        if (!joinManager.hasCollections()) {
            return getResultListViaObjectQuery(totalSize);
        } else {
            return getResultListViaIdQuery(totalSize);
        }
    }

    @Override
    public String getPageCountQueryString() {
        prepareAndCheck();
        return getPageCountQueryString0();
    }
    
    private String getPageCountQueryString0() {
        if (cachedCountQueryString == null) {
            cachedCountQueryString = getPageCountQueryString1();
        }
        
        return cachedCountQueryString;
    }

    @Override
    public String getPageIdQueryString() {
        prepareAndCheck();
        return getPageIdQueryString0();
    }
    
    private String getPageIdQueryString0() {
        if (cachedIdQueryString == null) {
            cachedIdQueryString = getPageIdQueryString1();
        }
        
        return cachedIdQueryString;
    }

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }
    
    private String getQueryString0() {
        if (cachedQueryString == null) {
            if (!joinManager.hasCollections()) {
                cachedQueryString = getObjectQueryString1();
            } else {
                cachedQueryString = getQueryString1();
            }
        }
        
        return cachedQueryString;
    }
    
    private void clearCache() {
        needsCheck = true;
        cachedCountQueryString = null;
        cachedIdQueryString = null;
        cachedQueryString = null;
    }

    private void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }
        
        verifyBuilderEnded();
        if (!orderByManager.hasOrderBys()) {
            throw new IllegalStateException("Pagination requires at least one order by item!");
        }

        applyImplicitJoins();
        applyExpressionTransformers();
        
        Metamodel m = em.getMetamodel();
        orderByExpressions = orderByManager.getRealExpressions(m);
        
        if (!orderByExpressions.get(orderByExpressions.size() - 1).isUnique()) {
            throw new IllegalStateException("The last order by item must be unique!");
        }
        
        needsNewIdList = extractKeySet || orderByManager.hasSubqueryOrderBys();
        keySetMode = KeySetPaginationHelper.getKeySetMode(extractKeySet, keySet, firstRow, pageSize, orderByExpressions);
        // TODO: replace this with needCheck = false as soon as mutation tracking #60 is done
        clearCache();
    }

    private PagedList<T> getResultListViaObjectQuery(long totalSize) {
        String queryString = getQueryString0();
        TypedQuery<T> query = (TypedQuery<T>) em.createQuery(queryString, Object[].class)
            .setMaxResults(pageSize);

        if (keySetMode == KeySetMode.NONE) {
            query.setFirstResult(firstRow);
        }
        
        KeySetExtractionObjectBuilder<T> objectBuilder = null;
        ObjectBuilder<T> transformerObjectBuilder = selectManager.getSelectObjectBuilder();
        
        if (extractKeySet) {
            int keySetSize = orderByExpressions.size();
            
            if (transformerObjectBuilder == null) {
                objectBuilder = new KeySetExtractionObjectBuilder<T>(keySetSize);
            } else {
                objectBuilder = new DelegatingKeySetExtractionObjectBuilder<T>(transformerObjectBuilder, keySetSize);
            }
            
            transformerObjectBuilder = objectBuilder;
        }
         
        if (transformerObjectBuilder != null) {
            for (QueryTransformer transformer : cbf.getQueryTransformers()) {
                transformer.transformQuery((TypedQuery<T>) query, transformerObjectBuilder);
            }
        }

        parameterizeQuery(query);
        List<T> result = query.getResultList();

        if (result.isEmpty()) {
            KeySet newKeySet = null;
            if (keySetMode == KeySetMode.NEXT) {
                // When we scroll over the last page to a non existing one, we reuse the current keyset
                newKeySet = keySet;
            }
            
            return new PagedListImpl<T>(newKeySet, totalSize);
        }
        
        KeySet newKeySet = null;

        if (extractKeySet) {
            Serializable[] lowest = objectBuilder.getLowest();
            Serializable[] highest = objectBuilder.getHighest();
            newKeySet = new KeySetImpl(firstRow, pageSize, orderByExpressions, lowest, highest);
        }

        PagedList<T> pagedResultList = new PagedListImpl<T>(result, newKeySet, totalSize);
        return pagedResultList;
    }

    private PagedList<T> getResultListViaIdQuery(long totalSize) {
        String idQueryString = getPageIdQueryString0();
        Query idQuery = em.createQuery(idQueryString)
            .setMaxResults(pageSize);

        if (keySetMode == KeySetMode.NONE) {
            idQuery.setFirstResult(firstRow);
        }

        parameterizeQuery(idQuery);
        List ids = idQuery.getResultList();

        if (ids.isEmpty()) {
            KeySet newKeySet = null;
            if (keySetMode == KeySetMode.NEXT) {
                // When we scroll over the last page to a non existing one, we reuse the current keyset
                newKeySet = keySet;
            }
            
            return new PagedListImpl<T>(newKeySet, totalSize);
        }

        Serializable[] lowest = null;
        Serializable[] highest = null;

        if (needsNewIdList) {
            if (extractKeySet) {
                lowest = KeySetPaginationHelper.extractKey((Object[]) ids.get(0), 1);
                highest = KeySetPaginationHelper.extractKey((Object[]) ids.get(ids.size() - 1), 1);
            }

            List newIds = new ArrayList(ids.size());

            for (int i = 0; i < ids.size(); i++) {
                newIds.add(((Object[]) ids.get(i))[0]);
            }

            ids = newIds;
        }

        parameterManager.addParameterMapping(idParamName, ids);

        KeySet newKeySet = null;

        if (extractKeySet) {
            newKeySet = new KeySetImpl(firstRow, pageSize, orderByExpressions, lowest, highest);
        }

        // TODO: replace this with super.getResultList() as soon as caching is implemented
        List<T> queryResultList = getQueryResultList();
        PagedList<T> pagedResultList = new PagedListImpl<T>(queryResultList, newKeySet, totalSize);
        return pagedResultList;
    }
    
    private List<T> getQueryResultList() {
        TypedQuery<T> query = (TypedQuery) em.createQuery(getQueryString0(), Object[].class);
        if (selectManager.getSelectObjectBuilder() != null) {
            transformQuery(query);
        }

        parameterizeQuery(query);
        return query.getResultList();
    }
    
    private String getPageCountQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();

        String idClause = new StringBuilder(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .toString();

        sbSelectFrom.append("SELECT COUNT(").append(idClause).append(')');
        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());

        StringBuilder sbRemaining = new StringBuilder();
        whereManager.buildClause(sbRemaining);
        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);

        joinManager.buildJoins(sbSelectFrom, false);
        addWhereClauseConjuncts(sbRemaining, false);

        return sbSelectFrom.append(sbRemaining).toString();
    }
    
    private String getPageIdQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();
        StringBuilder idClause = new StringBuilder(joinManager.getRootAlias())
            .append('.')
            .append(idName);

        sbSelectFrom.append("SELECT ")
            .append(idClause);
        
        if (needsNewIdList) {
            orderByManager.buildSelectClauses(sbSelectFrom, extractKeySet);
        }

        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());

        StringBuilder sbRemaining = new StringBuilder();

        if (keySetMode == KeySetMode.NONE) {
            whereManager.buildClause(sbRemaining);
        } else {
            sbRemaining.append(" WHERE ");
            applyKeySetClause(sbRemaining);

            if (whereManager.hasPredicates()) {
                sbRemaining.append(" AND (");
                whereManager.buildClausePredicate(sbRemaining);
                sbRemaining.append(')');
            }
        }

        sbRemaining.append(" GROUP BY ").append(idClause);
        
        boolean inverseOrder = keySetMode == KeySetMode.PREVIOUS;
        orderByManager.buildOrderBy(sbRemaining, inverseOrder);

        joinManager.buildJoins(sbSelectFrom, false);
        addWhereClauseConjuncts(sbRemaining, false);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager));

        return sbSelectFrom.append(sbRemaining).toString();
    }

    private String getQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();

        sbSelectFrom.append(selectManager.buildSelect(joinManager.getRootAlias()));
        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());

        StringBuilder sbRemaining = new StringBuilder();
        sbRemaining.append(" WHERE ")
            .append(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .append(" IN :")
            .append(idParamName)
            .append("");

        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);
        orderByManager.buildOrderBy(sbRemaining, false);

        joinManager.buildJoins(sbSelectFrom, true);

        return sbSelectFrom.append(sbRemaining).toString();
    }
    
    private String getObjectQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        sbSelectFrom.append(selectManager.buildSelect(joinManager.getRootAlias()));
        
        if (extractKeySet) {
            orderByManager.buildSelectClauses(sbSelectFrom, true);
        }
        
        sbSelectFrom.append(" FROM ")
            .append(fromClazz.getSimpleName())
            .append(' ')
            .append(joinManager.getRootAlias());

        StringBuilder sbRemaining = new StringBuilder();
        
        if (keySetMode == KeySetMode.NONE) {
            whereManager.buildClause(sbRemaining);
        } else {
            sbRemaining.append(" WHERE ");
            applyKeySetClause(sbRemaining);

            if (whereManager.hasPredicates()) {
                sbRemaining.append(" AND (");
                whereManager.buildClausePredicate(sbRemaining);
                sbRemaining.append(')');
            }
        }

        groupByManager.buildGroupBy(sbRemaining);
        havingManager.buildClause(sbRemaining);
        
        boolean inverseOrder = keySetMode == KeySetMode.PREVIOUS;
        orderByManager.buildOrderBy(sbRemaining, inverseOrder);

        joinManager.buildJoins(sbSelectFrom, false);
        addWhereClauseConjuncts(sbRemaining, false);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager));

        return sbSelectFrom.append(sbRemaining).toString();
    }
    
    private void applyKeySetClause(StringBuilder sbRemaining) {
        int expressionCount = orderByExpressions.size();
        Serializable[] key;

        if (keySetMode == KeySetMode.NEXT) {
            key = keySet.getHighest();
        } else {
            key = keySet.getLowest();
        }

        boolean generateEqualPredicate = true;
        int brackets = 0;
        
        // We wrap the whole thing in brackets
        brackets++;
        sbRemaining.append('(');
            
        for (int i = 0; i < expressionCount; i++) {
            boolean isNotLast = i + 1 != expressionCount;
            
            OrderByExpression orderByExpr = orderByExpressions.get(i);
            Expression expr = orderByExpr.getExpression();
            
            if (orderByExpr.isNullable()) {
                boolean isPrevious = keySetMode == KeySetMode.PREVIOUS;
                
                if (key[i] == null) {
                    if (orderByExpr.isNullFirst() == isPrevious) {
                        // Case for previous and null first or not previous and null last
                        generateEqualPredicate = false;
                        applyKeySetNullItem(sbRemaining, expr, false);
                    } else {
                        // Case for previous and null last or not previous and null first
                        applyKeySetNullItem(sbRemaining, expr, true);
                    }
                } else {
                    if (orderByExpr.isNullFirst() == isPrevious) {
                        // Case for previous and null first or not previous and null last
                        sbRemaining.append('(');
                        applyKeySetNotNullableItem(orderByExpr, sbRemaining, expr, i, key);
                        sbRemaining.append(" OR ");
                        applyKeySetNullItem(sbRemaining, expr, false);
                        sbRemaining.append(')');
                    } else {
                        // Case for previous and null last or not previous and null first
                        applyKeySetNotNullableItem(orderByExpr, sbRemaining, expr, i, key);
                    }
                }
            } else {
                applyKeySetNotNullableItem(orderByExpr, sbRemaining, expr, i, key);
            }
            
            if (isNotLast) {
                if (generateEqualPredicate) {
                    brackets++;
                    sbRemaining.append(" OR (");
                    if (key[i] == null) {
                        applyKeySetNullItem(sbRemaining, expr, false);
                    } else {
                        applyKeySetItem(sbRemaining, expr, "=", i, key[i]);
                    }
                }
                
                sbRemaining.append(" AND ");
                if (i + 2 != expressionCount) {
                    brackets++;
                    sbRemaining.append('(');
                }
                
                generateEqualPredicate = true;
            }
        }
        
        for (int i = 0; i < brackets; i++) {
            sbRemaining.append(')');
        }
    }

    private void applyKeySetNotNullableItem(OrderByExpression orderByExpr, StringBuilder sbRemaining, Expression expr, int i, Serializable[] key) {
        String operator;
        switch (keySetMode) {
            case SAME:
                operator = orderByExpr.isAscending() ? ">=" : "<=";
                break;
            case NEXT:
                operator = orderByExpr.isAscending() ? ">" : "<";
                break;
            case PREVIOUS:
                operator = orderByExpr.isAscending() ? "<" : ">";
                break;
            default:
                throw new IllegalArgumentException("Unknown key set mode: " + keySetMode);
        }
        
        applyKeySetItem(sbRemaining, expr, operator, i, key[i]);
    }
    
    private void applyKeySetItem(StringBuilder sbRemaining, Expression expr, String operator, int position, Serializable keyElement) {
        queryGenerator.setQueryBuffer(sbRemaining);
        expr.accept(queryGenerator);
        sbRemaining.append(" ");
        sbRemaining.append(operator);
        sbRemaining.append(" :");
        String parameterName = new StringBuilder(KEY_SET_PARAMETER_NAME).append('_').append(position).toString();
        sbRemaining.append(parameterName);
        parameterManager.addParameterMapping(parameterName, keyElement);
    }

    private void applyKeySetNullItem(StringBuilder sbRemaining, Expression expr, boolean not) {
        queryGenerator.setQueryBuffer(sbRemaining);
        expr.accept(queryGenerator);
        
        if (not) {
            sbRemaining.append(" IS NOT NULL");
        } else {
            sbRemaining.append(" IS NULL");
        }
    }

    @Override
    public PaginatedCriteriaBuilder<T> distinct() {
        throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public PaginatedCriteriaBuilder<T> groupBy(String... paths) {
        throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
    }

    @Override
    public PaginatedCriteriaBuilder<T> groupBy(String expression) {
        throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
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

    @Override
    public CaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>> selectCase() {
        return (CaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>>) super.selectCase();
    }

    @Override
    public CaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>> selectCase(String alias) {
        return (CaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>>) super.selectCase(alias);
    }

    @Override
    public SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>> selectSimpleCase(String expression) {
        return (SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>>) super.selectSimpleCase(expression);
    }

    @Override
    public SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>> selectSimpleCase(String expression, String alias) {
        return (SimpleCaseWhenBuilder<PaginatedCriteriaBuilder<Tuple>>) super.selectSimpleCase(expression, alias);
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>> selectSubquery() {
        return (SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>>) super.selectSubquery();
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>> selectSubquery(String alias) {
        return (SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>>) super.selectSubquery(alias);
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>> selectSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>>) super.selectSubquery(subqueryAlias, expression);
    }

    @Override
    public SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
        return (SubqueryInitiator<PaginatedCriteriaBuilder<Tuple>>) super.selectSubquery(subqueryAlias, expression, selectAlias);
    }
}
