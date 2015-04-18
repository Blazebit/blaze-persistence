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
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractQueryBuilder<T, X extends QueryBuilder<T, X>> extends AbstractBaseQueryBuilder<T, X> implements
    QueryBuilder<T, X> {

    /**
     * This flag indicates whether the current builder has been used to create a
     * PaginatedCriteriaBuilder. In this case we must not allow any calls to
     * group by and distinct since the corresponding managers are shared with
     * the PaginatedCriteriaBuilder and any changes would affect the
     * PaginatedCriteriaBuilder as well.
     */
    private boolean createdPaginatedBuilder = false;

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractQueryBuilder(AbstractQueryBuilder<T, ? extends QueryBuilder<T, ?>> builder) {
        super(builder);
    }

    public AbstractQueryBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        super(cbf, em, clazz, alias, registeredFunctions);
    }

    @Override
    public List<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public T getSingleResult() {
        return getQuery().getSingleResult();
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize) {
        clearCache();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, null, firstRow, pageSize);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(Object entityId, int pageSize) {
        clearCache();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        checkEntityId(entityId);
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, false, null, entityId, pageSize);
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(KeysetPage keysetPage, int firstRow, int pageSize) {
        clearCache();
        if (selectManager.isDistinct()) {
            throw new IllegalStateException("Cannot paginate a DISTINCT query");
        }
        if (!groupByManager.getGroupByInfos().isEmpty()) {
            throw new IllegalStateException("Cannot paginate a GROUP BY query");
        }
        createdPaginatedBuilder = true;
        return new PaginatedCriteriaBuilderImpl<T>(this, true, keysetPage, firstRow, pageSize);
    }
    
    private void checkEntityId(Object entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Invalid null entity id given");
        } 
        
        EntityType<?> entityType = fromClazz;
        Class<?> idType = entityType.getIdType().getJavaType();
        
        if (!idType.isInstance(entityId)) {
            throw new IllegalArgumentException("The type of the given entity id '" + entityId.getClass().getName() + "' is not an instance of the expected id type '" + idType.getName() + "' of the entity class '" + fromClazz.getName() + "'");
        }
    }

    @Override
    public X setParameter(String name, Object value) {
        parameterManager.satisfyParameter(name, value);
        return (X) this;
    }

    @Override
    public X setParameter(String name, Calendar value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalCalendarParameterWrapper(value, temporalType));
        return (X) this;
    }

    @Override
    public X setParameter(String name, Date value, TemporalType temporalType) {
        parameterManager.satisfyParameter(name, new ParameterManager.TemporalDateParameterWrapper(value, temporalType));
        return (X) this;
    }

    @Override
    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Class<Y> clazz) {
        clearCache();
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor) {
        clearCache();
        if (constructor == null) {
            throw new NullPointerException("constructor");
        }

        verifyBuilderEnded();
        return selectManager.selectNew(this, constructor);
    }

    @Override
    public <Y> QueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> objectBuilder) {
        clearCache();
        if (objectBuilder == null) {
            throw new NullPointerException("objectBuilder");
        }

        verifyBuilderEnded();
        selectManager.selectNew(this, objectBuilder);
        return (QueryBuilder<Y, ?>) this;
    }

    private void checkFetchJoinAllowed() {
        if (selectManager.getSelectInfos().size() > 0) {
            throw new IllegalStateException("Fetch joins are only possible if the root entity is selected");
        }
    }

    @Override
    public X fetch(String path) {
        clearCache();
        checkFetchJoinAllowed();
        verifyBuilderEnded();
        joinManager.implicitJoin(expressionFactory.createSimpleExpression(path), true, null, false, false, true);
        return (X) this;
    }

    @Override
    public X innerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.INNER, true);
    }

    @Override
    public X innerJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.INNER, true);
    }

    @Override
    public X leftJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.LEFT, true);
    }

    @Override
    public X leftJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.LEFT, true);
    }

    @Override
    public X rightJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public X rightJoinFetchDefault(String path, String alias) {
        return joinDefault(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public X join(String path, String alias, JoinType type, boolean fetch) {
        return join(path, alias, type, fetch, false);
    }

    @Override
    public X joinDefault(String path, String alias, JoinType type, boolean fetch) {
        return join(path, alias, type, fetch, true);
    }

    private X join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        clearCache();
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (alias == null) {
            throw new NullPointerException("alias");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (alias.isEmpty()) {
            throw new IllegalArgumentException("Empty alias");
        }

        if (fetch == true) {
            checkFetchJoinAllowed();
        }

        verifyBuilderEnded();
        joinManager.join(path, alias, type, fetch, defaultJoin);
        return (X) this;
    }

    @Override
    public TypedQuery<T> getQuery() {
        TypedQuery<T> query = (TypedQuery) em.createQuery(getQueryString(), selectManager.getExpectedQueryResultType());
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }

        parameterizeQuery(query);
        return query;
    }

    void parameterizeQuery(Query q) {
        for (Parameter<?> p : q.getParameters()) {
            if (!isParameterSet(p.getName())) {
                throw new IllegalStateException("Unsatisfied parameter " + p.getName());
            }
            Object paramValue = parameterManager.getParameterValue(p.getName());
            if (paramValue instanceof ParameterManager.TemporalCalendarParameterWrapper) {
                ParameterManager.TemporalCalendarParameterWrapper wrappedValue = (ParameterManager.TemporalCalendarParameterWrapper) paramValue;
                q.setParameter(p.getName(), wrappedValue.getValue(), wrappedValue.getType());
            } else if (paramValue instanceof ParameterManager.TemporalDateParameterWrapper) {
                ParameterManager.TemporalDateParameterWrapper wrappedValue = (ParameterManager.TemporalDateParameterWrapper) paramValue;
                q.setParameter(p.getName(), wrappedValue.getValue(), wrappedValue.getType());
            } else {
                q.setParameter(p.getName(), paramValue);
            }
        }
    }

    @Override
    public boolean containsParameter(String name) {
        return parameterManager.containsParameter(name);
    }

    @Override
    public boolean isParameterSet(String name) {
        return parameterManager.isParameterSet(name);
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return parameterManager.getParameter(name);
    }

    @Override
    public Set<? extends Parameter<?>> getParameters() {
        return parameterManager.getParameters();
    }

    @Override
    public Object getParameterValue(String name) {
        return parameterManager.getParameterValue(name);
    }

    @Override
    public Metamodel getMetamodel() {
        return em.getMetamodel();
    }

    @Override
    public X groupBy(String expression) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.groupBy(expression);
    }

    @Override
    public X groupBy(String... paths) {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling groupBy() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.groupBy(paths);
    }

    @Override
    public X distinct() {
        if (createdPaginatedBuilder) {
            throw new IllegalStateException("Calling distinct() on a PaginatedCriteriaBuilder is not allowed.");
        }
        return super.distinct();
    }

    @Override
    public CaseWhenBuilder<? extends QueryBuilder<T, ?>> selectCase() {
        return (CaseWhenBuilder<QueryBuilder<T, ?>>) super.selectCase();
    }

    @Override
    public CaseWhenBuilder<? extends QueryBuilder<T, ?>> selectCase(String alias) {
        return (CaseWhenBuilder<QueryBuilder<T, ?>>) super.selectCase(alias);
    }

    @Override
    public SimpleCaseWhenBuilder<? extends QueryBuilder<T, ?>> selectSimpleCase(String expression) {
        return (SimpleCaseWhenBuilder<QueryBuilder<T, ?>>) super.selectSimpleCase(expression);
    }

    @Override
    public SimpleCaseWhenBuilder<? extends QueryBuilder<T, ?>> selectSimpleCase(String expression, String alias) {
        return (SimpleCaseWhenBuilder<QueryBuilder<T, ?>>) super.selectSimpleCase(expression, alias);
    }

    @Override
    public QueryBuilder<T, ?> select(String expression) {
        return (QueryBuilder<T, ?>) super.select(expression);
    }

    @Override
    public QueryBuilder<T, ?> select(String expression, String alias) {
        return (QueryBuilder<T, ?>) super.select(expression, alias);
    }

    @Override
    public SubqueryInitiator<? extends QueryBuilder<T, ?>> selectSubquery() {
        return (SubqueryInitiator<? extends QueryBuilder<T, ?>>) super.selectSubquery();
    }

    @Override
    public SubqueryInitiator<? extends QueryBuilder<T, ?>> selectSubquery(String alias) {
        return (SubqueryInitiator<? extends QueryBuilder<T, ?>>) super.selectSubquery(alias);
    }

    @Override
    public SubqueryInitiator<? extends QueryBuilder<T, ?>> selectSubquery(String subqueryAlias, String expression) {
        return (SubqueryInitiator<? extends QueryBuilder<T, ?>>) super.selectSubquery(subqueryAlias, expression);
    }

    @Override
    public SubqueryInitiator<? extends QueryBuilder<T, ?>> selectSubquery(String subqueryAlias, String expression, String selectAlias) {
        return (SubqueryInitiator<? extends QueryBuilder<T, ?>>) super.selectSubquery(subqueryAlias, expression, selectAlias);
    }

}
