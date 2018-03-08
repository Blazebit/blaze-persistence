/*
 * Copyright 2014 - 2018 Blazebit.
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TypedQueryWrapper<X> implements TypedQuery<X> {
    
    protected final TypedQuery<X> delegate;

    public TypedQueryWrapper(TypedQuery<X> delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<X> getResultList() {
        return delegate.getResultList();
    }

    @Override
    public X getSingleResult() {
        return delegate.getSingleResult();
    }

    @Override
    public TypedQuery<X> setMaxResults(int i) {
        delegate.setMaxResults(i);
        return this;
    }

    @Override
    public TypedQuery<X> setFirstResult(int i) {
        delegate.setFirstResult(i);
        return this;
    }

    @Override
    public TypedQuery<X> setHint(String string, Object o) {
        delegate.setHint(string, o);
        return this;
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> prmtr, T t) {
        // required for Hibernate 4.2
        if (prmtr.getName() == null) {
            delegate.setParameter(prmtr, t);
        } else {
            delegate.setParameter(prmtr.getName(), t);
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> prmtr, Calendar clndr, TemporalType tt) {
        delegate.setParameter(prmtr, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> prmtr, Date date, TemporalType tt) {
        delegate.setParameter(prmtr, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Object o) {
        delegate.setParameter(string, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Calendar clndr, TemporalType tt) {
        delegate.setParameter(string, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Date date, TemporalType tt) {
        delegate.setParameter(string, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Object o) {
        delegate.setParameter(i, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Calendar clndr, TemporalType tt) {
        delegate.setParameter(i, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Date date, TemporalType tt) {
        delegate.setParameter(i, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType fmt) {
        delegate.setFlushMode(fmt);
        return this;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lmt) {
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
        return delegate.getParameters();
    }

    @Override
    public Parameter<?> getParameter(String string) {
        return delegate.getParameter(string);
    }

    @Override
    public <T> Parameter<T> getParameter(String string, Class<T> type) {
        return delegate.getParameter(string, type);
    }

    @Override
    public Parameter<?> getParameter(int i) {
        return delegate.getParameter(i);
    }

    @Override
    public <T> Parameter<T> getParameter(int i, Class<T> type) {
        return delegate.getParameter(i, type);
    }

    @Override
    public boolean isBound(Parameter<?> prmtr) {
        return delegate.isBound(prmtr);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> prmtr) {
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
}
