/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.impl.query;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.criteria.ParameterExpression;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class QueryWrapper implements Query {
    
    protected final Query delegate;
    protected final Map<Parameter<?>, String> parameterNameMapping;

    public QueryWrapper(Query delegate, Map<ParameterExpression<?>, String> criteriaNameMapping) {
        this.delegate = delegate;
        if (criteriaNameMapping == null) {
            this.parameterNameMapping = null;
        } else {
            Set<Parameter<?>> parameters = delegate.getParameters();
            Map<Parameter<?>, String> parameterNameMapping = new HashMap<>(parameters.size());
            Map<String, Parameter<?>> parameterMapping = new HashMap<>(parameters.size());
            for (Parameter<?> parameter : parameters) {
                parameterNameMapping.put(parameter, parameter.getName());
                parameterMapping.put(parameter.getName(), parameter);
            }
            for (Map.Entry<ParameterExpression<?>, String> entry : criteriaNameMapping.entrySet()) {
                Parameter<?> parameter = parameterMapping.get(entry.getValue());
                if (parameter != null) {
                    parameterNameMapping.remove(parameter);
                    parameterNameMapping.put(entry.getKey(), entry.getValue());
                }
            }
            this.parameterNameMapping = parameterNameMapping;
        }
    }

    public Query getDelegate() {
        return delegate;
    }

    @Override
    public List getResultList() {
        return delegate.getResultList();
    }

    @Override
    public Object getSingleResult() {
        return delegate.getSingleResult();
    }

    @Override
    public Query setMaxResults(int i) {
        delegate.setMaxResults(i);
        return this;
    }

    @Override
    public Query setFirstResult(int i) {
        delegate.setFirstResult(i);
        return this;
    }

    @Override
    public Query setHint(String string, Object o) {
        delegate.setHint(string, o);
        return this;
    }

    @Override
    public <T> Query setParameter(Parameter<T> prmtr, T t) {
        String name = parameterNameMapping == null ? prmtr.getName() : parameterNameMapping.get(prmtr);
        // required for Hibernate 4.2
        if (name == null) {
            delegate.setParameter(prmtr, t);
        } else {
            delegate.setParameter(name, t);
        }
        return this;
    }

    @Override
    public Query setParameter(Parameter<Calendar> prmtr, Calendar clndr, TemporalType tt) {
        String name = parameterNameMapping == null ? prmtr.getName() : parameterNameMapping.get(prmtr);
        // required for Hibernate 4.2
        if (name == null) {
            delegate.setParameter(prmtr, clndr, tt);
        } else {
            delegate.setParameter(name, clndr, tt);
        }
        return this;
    }

    @Override
    public Query setParameter(Parameter<Date> prmtr, Date date, TemporalType tt) {
        String name = parameterNameMapping == null ? prmtr.getName() : parameterNameMapping.get(prmtr);
        // required for Hibernate 4.2
        if (name == null) {
            delegate.setParameter(prmtr, date, tt);
        } else {
            delegate.setParameter(name, date, tt);
        }
        return this;
    }

    @Override
    public Query setParameter(String string, Object o) {
        delegate.setParameter(string, o);
        return this;
    }

    @Override
    public Query setParameter(String string, Calendar clndr, TemporalType tt) {
        delegate.setParameter(string, clndr, tt);
        return this;
    }

    @Override
    public Query setParameter(String string, Date date, TemporalType tt) {
        delegate.setParameter(string, date, tt);
        return this;
    }

    @Override
    public Query setParameter(int i, Object o) {
        delegate.setParameter(i, o);
        return this;
    }

    @Override
    public Query setParameter(int i, Calendar clndr, TemporalType tt) {
        delegate.setParameter(i, clndr, tt);
        return this;
    }

    @Override
    public Query setParameter(int i, Date date, TemporalType tt) {
        delegate.setParameter(i, date, tt);
        return this;
    }

    @Override
    public Query setFlushMode(FlushModeType fmt) {
        delegate.setFlushMode(fmt);
        return this;
    }

    @Override
    public Query setLockMode(LockModeType lmt) {
        delegate.setLockMode(lmt);
        return this;
    }

    @Override
    public int executeUpdate() {
        return delegate.executeUpdate();
    }

    @Override
    public int getMaxResults() {
        return delegate.getMaxResults();
    }

    @Override
    public int getFirstResult() {
        return delegate.getFirstResult();
    }

    @Override
    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        if (parameterNameMapping == null) {
            return delegate.getParameters();
        } else {
            return parameterNameMapping.keySet();
        }
    }

    @Override
    public Parameter<?> getParameter(String string) {
        if (parameterNameMapping != null) {
            for (Map.Entry<Parameter<?>, String> entry : parameterNameMapping.entrySet()) {
                if (string.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return delegate.getParameter(string);
    }

    @Override
    public <T> Parameter<T> getParameter(String string, Class<T> type) {
        if (parameterNameMapping != null) {
            for (Map.Entry<Parameter<?>, String> entry : parameterNameMapping.entrySet()) {
                if (string.equals(entry.getValue())) {
                    if (!entry.getKey().getParameterType().isAssignableFrom(type)) {
                        throw new IllegalArgumentException("Parameter '" + string + "' is not assignable to '" + type.getName() + "'!");
                    }
                    return (Parameter<T>) entry.getKey();
                }
            }
        }
        return delegate.getParameter(string, type);
    }

    @Override
    public Parameter<?> getParameter(int i) {
        if (parameterNameMapping != null) {
            String string = Integer.toString(i);
            for (Map.Entry<Parameter<?>, String> entry : parameterNameMapping.entrySet()) {
                if (string.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return delegate.getParameter(i);
    }

    @Override
    public <T> Parameter<T> getParameter(int i, Class<T> type) {
        if (parameterNameMapping != null) {
            String string = Integer.toString(i);
            for (Map.Entry<Parameter<?>, String> entry : parameterNameMapping.entrySet()) {
                if (string.equals(entry.getValue())) {
                    if (!entry.getKey().getParameterType().isAssignableFrom(type)) {
                        throw new IllegalArgumentException("Parameter at position " + i + " is not assignable to '" + type.getName() + "'!");
                    }
                    return (Parameter<T>) entry.getKey();
                }
            }
        }
        return delegate.getParameter(i, type);
    }

    @Override
    public boolean isBound(Parameter<?> prmtr) {
        if (parameterNameMapping != null) {
            String name = parameterNameMapping.get(prmtr);
            return delegate.isBound(delegate.getParameter(name));
        }
        return delegate.isBound(prmtr);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> prmtr) {
        if (parameterNameMapping != null) {
            String name = parameterNameMapping.get(prmtr);
            return (T) delegate.getParameterValue(name);
        }
        return delegate.getParameterValue(prmtr);
    }

    @Override
    public Object getParameterValue(String string) {
        return delegate.getParameterValue(string);
    }

    @Override
    public Object getParameterValue(int i) {
        return delegate.getParameterValue(i);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return delegate.unwrap(type);
    }

    public Stream getResultStream() {
        return delegate.getResultStream();
    }

}
