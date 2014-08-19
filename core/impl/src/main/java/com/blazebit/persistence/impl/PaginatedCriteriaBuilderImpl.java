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
    private String[] orderByExpressionStrings;
    private KeySetMode keySetMode = KeySetMode.NONE;
    private boolean needsNewIdList = false;
    
    private static enum KeySetMode {
        NONE,
        SAME,
        NEXT,
        PREVIOUS;
    }

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
    
    private KeySetMode getKeySetMode() {
        // key set pagination must be activated and a key set must be given
        if (!extractKeySet || keySet == null) {
            return KeySetMode.NONE;
        }
        // The last page size must equal the current page size
        if (keySet.getMaxResults() != pageSize) {
            return KeySetMode.NONE;
        }
        // Ordering has changed
        if (!Arrays.equals(keySet.getOrderByExpressions(), orderByExpressionStrings)) {
            return KeySetMode.NONE;
        }
        
        int offset = keySet.getFirstResult() - firstRow;
        
        if (offset == pageSize) {
            // We went to the previous page
            if (isValidKey(keySet.getLowest())) {
                return KeySetMode.PREVIOUS;
            } else {
                return KeySetMode.NONE;
            }
        } else if (offset == -pageSize) {
            // We went to the next page
            if (isValidKey(keySet.getHighest())) {
                return KeySetMode.NEXT;
            } else {
                return KeySetMode.NONE;
            }
        } else if (offset == 0) {
            // Same page again
            if (isValidKey(keySet.getLowest())) {
                return KeySetMode.SAME;
            } else {
                return KeySetMode.NONE;
            }
        } else {
            // The last key set is away more than one page
            return KeySetMode.NONE;
        }
    }
    
    private boolean isValidKey(Serializable[] key) {
        return key != null && key.length == orderByExpressionStrings.length;
    }

    @Override
    public PagedList<T> getResultList() {
        if (!orderByManager.hasOrderBys()) {
            throw new IllegalStateException("Pagination requires at least one order by item!");
        }
        
        String countQueryString = getPageCountQueryString();
        TypedQuery<Long> countQuery = em.createQuery(countQueryString, Long.class);
        parameterizeQuery(countQuery);

        long totalSize = countQuery.getSingleResult();

        if (totalSize == 0L) {
            return new PagedListImpl<T>(totalSize);
        }

        String idQueryString = getPageIdQueryString();
        Query idQuery = em.createQuery(idQueryString)
            .setMaxResults(pageSize);
        
        if (keySetMode == KeySetMode.NONE) {
            idQuery.setFirstResult(firstRow);
        }

        parameterizeQuery(idQuery);
        List ids = idQuery.getResultList();

        if (ids.isEmpty()) {
            // TODO: maybe we need to add the previous keyset here?
            return new PagedListImpl<T>(totalSize);
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
    
    private Serializable[] extractKey(Object[] tuple, int offset) {
        Serializable[] key = new Serializable[tuple.length - offset];
        System.arraycopy(tuple, offset, key, 0, key.length);
        return key;
    }

    @Override
    public String getPageCountQueryString() {
        verifyBuilderEnded();
        StringBuilder sbSelectFrom = new StringBuilder();

        applyImplicitJoins();
        applyExpressionTransformers();

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

        StringBuilder sbJoin = new StringBuilder();
        joinManager.buildJoins(false, sbJoin);

        return sbSelectFrom.append(sbJoin).append(sbRemaining).toString();
    }

    @Override
    public String getQueryString() {
        verifyBuilderEnded();
        StringBuilder sbSelectFrom = new StringBuilder();
        applyImplicitJoins();
        applyExpressionTransformers();

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
        applyExpressionTransformers();

        String idClause = new StringBuilder(joinManager.getRootAlias())
            .append('.')
            .append(idName)
            .toString();

        sbSelectFrom.append("SELECT ")
            .append(idClause);

        orderByExpressionStrings = orderByManager.getAbsoluteExpressionStrings();
        keySetMode = getKeySetMode();
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

        StringBuilder sbJoin = new StringBuilder();
        joinManager.buildJoins(false, sbJoin);

        // execute illegal collection access check
        orderByManager.acceptVisitor(new IllegalSubqueryDetector(aliasManager, baseBuilder));
        
        return sbSelectFrom.append(sbJoin).append(sbRemaining).toString();
    }

    private void applyKeySetClause(StringBuilder sbRemaining, KeySetMode keySetMode, List<Expression> realExpressions, KeySet keySet) {
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
                Expression expr = realExpressions.get(i);
                applyKeySetItem(0, sbRemaining, expr, ">=", i, false, key[i]);
            }
            
            sbRemaining.append(')');
            
            return;
        }
        
        if (keySetMode == KeySetMode.NEXT) {
            // order by items must be > keySet.getHighest()
            // (x,y) > (a,b) => (x > a OR (x = a AND y > b) )
            // (x,y,z) > (a,b,c) => (x > a OR (x = a AND (y > b OR (y = b AND z > c) ) ) )
            operator = ">";
            key = keySet.getHighest();
        } else {
            // order by items must be < keySet.getLowest()
            // (x,y) < (a,b) => (x < a OR (x = a AND y < b) )
            // (x,y,z) < (a,b,c) => (x < a OR (x = a AND (y < b OR (y = b AND z < c) ) ) )
            operator = "<";
            key = keySet.getLowest();
        }
        
        int expressionCount = realExpressions.size();
        int brackets = 0;
        for (int i = 0; i < expressionCount; i++) {
            boolean openBracket = i + 1 != expressionCount;
            
            if (i != 0) {
                brackets++;
                sbRemaining.append(" OR (");
                Expression expr = realExpressions.get(i - 1);
                
                brackets = applyKeySetItem(brackets, sbRemaining, expr, "=", i - 1, openBracket, key[i - 1]);
                sbRemaining.append(" AND ");
            }
            Expression expr = realExpressions.get(i);
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
