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
    private final int firstRow;
    private final int pageSize;
    private final AbstractQueryBuilder<T, ? extends QueryBuilder<T, ?>> baseBuilder;
//    private String[] orderByExpressionStrings;
//    private KeySetMode keySetMode = KeySetMode.NONE;
    private PaginationStrategy paginationStrategy;
    private boolean needsNewIdList = false;

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
        this.baseBuilder = baseBuilder;
    }

    private void prepareAndCheck() {
        verifyBuilderEnded();
        if (!orderByManager.hasOrderBys()) {
            throw new IllegalStateException("Pagination requires at least one order by item!");
        }

        applyImplicitJoins();
        applyExpressionTransformers();
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

        return getResultListViaIdQuery(totalSize);
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
                lowest = extractKey((Object[]) ids.get(0), 1);
                highest = extractKey((Object[]) ids.get(ids.size() - 1), 1);
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
            newKeySet = new KeySetImpl(firstRow, pageSize, orderByExpressionStrings, lowest, highest);
        }

        PagedList<T> pagedResultList = new PagedListImpl<T>(super.getResultList(), newKeySet, totalSize);
        return pagedResultList;
    }

    @Override
    public String getPageCountQueryString() {
        prepareAndCheck();
        return getPageCountQueryString0();
    }
        
    private String getPageCountQueryString0() {
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

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }

    private String getQueryString0() {
        StringBuilder sbSelectFrom = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType()
            .getJavaType())
            .getName();

        sbSelectFrom.append(selectManager.buildSelect(joinManager.getRootAlias()));
        sbSelectFrom.append("FROM ")
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
        orderByManager.buildOrderBy(sbRemaining);

        joinManager.buildJoins(sbSelectFrom, true);

        return sbSelectFrom.append(sbRemaining).toString();
    }

    @Override
    public String getPageIdQueryString() {
        prepareAndCheck();
        return getPageIdQueryString0();
    }
    
    private String getPageIdQueryString0() {
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

        sbSelectFrom.append("SELECT ")
            .append(idClause);

//        orderByExpressionStrings = orderByManager.getAbsoluteExpressionStrings();
//        keySetMode = getKeySetMode(extractKeySet, keySet, firstRow, pageSize, orderByExpressionStrings);
        paginationStrategy = PaginationHelper.determinePaginationStrategy(extractKeySet, keySet, firstRow, pageSize, orderByManager.getRealExpressions());
        if (needsNewIdList = orderByManager.hasOrderBys(extractKeySet)) {
            sbSelectFrom.append(", ");
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
            applyKeySetClause(sbRemaining, keySetMode, orderByManager.getRealExpressions(), keySet);

            if (whereManager.hasPredicates()) {
                sbRemaining.append(" AND (");
                whereManager.buildClausePredicate(sbRemaining);
                sbRemaining.append(')');
            }
        }

        sbRemaining.append(" GROUP BY ").append(idClause);
        orderByManager.buildOrderBy(sbRemaining);

        joinManager.buildJoins(sbSelectFrom, false);
        addWhereClauseConjuncts(sbRemaining, false);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager, baseBuilder));

        return sbSelectFrom.append(sbRemaining).toString();
    }

    private Serializable[] extractKey(Object[] tuple, int offset) {
        Serializable[] key = new Serializable[tuple.length - offset];
        System.arraycopy(tuple, offset, key, 0, key.length);
        return key;
    }
    
    private void applyKeySetClause(StringBuilder sbRemaining, KeySetMode keySetMode, List<OrderByManager.OrderByExpression> realExpressions, KeySet keySet) {
        String operator;
        Serializable[] key;

        if (keySetMode == KeySetMode.SAME) {
            key = keySet.getLowest();

            int expressionCount = realExpressions.size();
            sbRemaining.append('(');

            for (int i = 0; i < expressionCount; i++) {
                if (i != 0) {
                    sbRemaining.append(" AND ");
                }
                OrderByManager.OrderByExpression orderByExpr = realExpressions.get(i);
                Expression expr = orderByExpr.getExpression();
                if (orderByExpr.isAscending()) {
                    operator = ">=";
                } else {
                    operator = "<=";
                }
                applyKeySetItem(0, sbRemaining, expr, operator, i, false, key[i]);
            }

            sbRemaining.append(')');
            return;
        }

        if (keySetMode == KeySetMode.NEXT) {
            // order by items must be > keySet.getHighest()
            // (x,y) > (a,b) => (x > a OR (x = a AND y > b) )
            // (x,y,z) > (a,b,c) => (x > a OR (x = a AND (y > b OR (y = b AND z > c) ) ) )
            key = keySet.getHighest();
        } else {
            // order by items must be < keySet.getLowest()
            // (x,y) < (a,b) => (x < a OR (x = a AND y < b) )
            // (x,y,z) < (a,b,c) => (x < a OR (x = a AND (y < b OR (y = b AND z < c) ) ) )
            key = keySet.getLowest();
        }

        int expressionCount = realExpressions.size();
        int brackets = 0;
        for (int i = 0; i < expressionCount; i++) {
            boolean openBracket = i + 1 != expressionCount;

            if (i != 0) {
                brackets++;
                sbRemaining.append(" OR (");
                Expression expr = realExpressions.get(i - 1).getExpression();

                brackets = applyKeySetItem(brackets, sbRemaining, expr, "=", i - 1, openBracket, key[i - 1]);
                sbRemaining.append(" AND ");
            }
            OrderByManager.OrderByExpression orderByExpr = realExpressions.get(i);
            Expression expr = realExpressions.get(i).getExpression();
            
            if ((keySetMode == KeySetMode.NEXT) ^ !orderByExpr.isAscending()) {
                operator = ">";
            } else {
                operator = "<";
            }
            brackets = applyKeySetItem(brackets, sbRemaining, expr, operator, i, openBracket, key[i]);
        }
        for (int i = 0; i < brackets; i++) {
            sbRemaining.append(')');
        }
    }

    private int applyKeySetItem(int brackets, StringBuilder sbRemaining, Expression expr, String operator, int position, boolean openBracket, Serializable keyElement) {
        if (openBracket) {
            brackets++;
            sbRemaining.append("(");
        }

        queryGenerator.setQueryBuffer(sbRemaining);
        expr.accept(queryGenerator);
        sbRemaining.append(" ");
        sbRemaining.append(operator);
        sbRemaining.append(" :");
        String parameterName = new StringBuilder(KEY_SET_PARAMETER_NAME).append('_').append(position).toString();
        sbRemaining.append(parameterName);
        parameterManager.addParameterMapping(parameterName, keyElement);
        return brackets;
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
