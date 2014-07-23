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

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Moritz Becker
 * @author Christian Beikov
 */
public abstract class AbstractQueryBuilder<T, X extends QueryBuilder<T, X>> extends AbstractBaseQueryBuilder<T, X> implements QueryBuilder<T, X> {

    /**
     * Create flat copy of builder
     *
     * @param builder
     */
    protected AbstractQueryBuilder(AbstractQueryBuilder<T, ? extends QueryBuilder<T, ?>> builder) {
        super(builder);
    }

    public AbstractQueryBuilder(EntityManager em, Class<T> clazz, String alias) {
        super(em, clazz, alias);
    }

    @Override
    public List<T> getResultList() {
        return getQuery().getResultList();
    }

    @Override
    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize) {
        return new PaginatedCriteriaBuilderImpl<T>(this, firstRow, pageSize);
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
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        
        verifyBuilderEnded();
        resultClazz = (Class<T>) clazz;
        return selectManager.selectNew(this, clazz);
    }

    @Override
    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor) {
        if (constructor == null) {
            throw new NullPointerException("constructor");
        }
        
        verifyBuilderEnded();
        resultClazz = (Class<T>) constructor.getDeclaringClass();
        return selectManager.selectNew(this, constructor);
    }

    @Override
    public <Y> QueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> objectBuilder) {
        if (objectBuilder == null) {
            throw new NullPointerException("objectBuilder");
        }
        
        verifyBuilderEnded();
        selectManager.selectNew(objectBuilder);
        return (QueryBuilder<Y, ?>) this;
    }

    @Override
    public X innerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.INNER, true);
    }

    @Override
    public X leftJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.LEFT, true);
    }

    @Override
    public X rightJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.RIGHT, true);
    }

    @Override
    public X outerJoinFetch(String path, String alias) {
        return join(path, alias, JoinType.OUTER, true);
    }

    @Override
    public X join(String path, String alias, JoinType type, boolean fetch) {
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
        
        verifyBuilderEnded();
        joinManager.join(path, alias, type, fetch);
        return (X) this;
    }

    @Override
    public TypedQuery<T> getQuery() {
        TypedQuery<T> query = (TypedQuery) em.createQuery(getQueryString(), Object[].class);
        if (selectManager.getSelectObjectBuilder() != null) {
            queryTransformer.transformQuery(query, selectManager.getSelectObjectBuilder());
        }

        parameterizeQuery(query);
        return query;
    }

    void parameterizeQuery(javax.persistence.Query q) {
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
    public QueryBuilder<Tuple, ?> select(String expression) {
        return (QueryBuilder<Tuple, ?>) super.select(expression);
    }

    @Override
    public QueryBuilder<Tuple, ?> select(String expression, String alias) {
        return (QueryBuilder<Tuple, ?>) super.select(expression, alias);
    }
}
