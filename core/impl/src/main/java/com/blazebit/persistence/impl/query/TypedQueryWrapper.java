/*
 * Copyright 2014 - 2021 Blazebit.
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
import java.util.stream.Stream;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TypedQueryWrapper<X> extends QueryWrapper implements TypedQuery<X> {
    
    public TypedQueryWrapper(TypedQuery<X> delegate, Map<ParameterExpression<?>, String> criteriaNameMapping) {
        super(delegate, criteriaNameMapping);
    }

    @Override
    public TypedQuery<X> getDelegate() {
        return (TypedQuery<X>) delegate;
    }

    @Override
    public List<X> getResultList() {
        return delegate.getResultList();
    }

    @Override
    public X getSingleResult() {
        return (X) delegate.getSingleResult();
    }

    @Override
    public TypedQuery<X> setMaxResults(int i) {
        super.setMaxResults(i);
        return this;
    }

    @Override
    public TypedQuery<X> setFirstResult(int i) {
        super.setFirstResult(i);
        return this;
    }

    @Override
    public TypedQuery<X> setHint(String string, Object o) {
        super.setHint(string, o);
        return this;
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> prmtr, T t) {
        super.setParameter(prmtr, t);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> prmtr, Calendar clndr, TemporalType tt) {
        super.setParameter(prmtr, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> prmtr, Date date, TemporalType tt) {
        super.setParameter(prmtr, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Object o) {
        super.setParameter(string, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Calendar clndr, TemporalType tt) {
        super.setParameter(string, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String string, Date date, TemporalType tt) {
        super.setParameter(string, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Object o) {
        super.setParameter(i, o);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Calendar clndr, TemporalType tt) {
        super.setParameter(i, clndr, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int i, Date date, TemporalType tt) {
        super.setParameter(i, date, tt);
        return this;
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType fmt) {
        super.setFlushMode(fmt);
        return this;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lmt) {
        super.setLockMode(lmt);
        return this;
    }

    public Stream<X> getResultStream() {
        return delegate.getResultStream();
    }

}
