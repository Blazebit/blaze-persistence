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

import com.blazebit.persistence.impl.ParameterValueTransformer;
import com.blazebit.persistence.impl.ValuesParameterBinder;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSQLQuery extends AbstractCustomQuery<Object> {

    private final Query delegate;

    public CustomSQLQuery(QuerySpecification querySpecification, Query delegate, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        super(querySpecification, transformers, valuesParameters, valuesBinders);
        this.delegate = delegate;
    }

    public Map<String, String> getAddedCtes() {
        return querySpecification.getAddedCtes();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getResultList() {
        throw new IllegalArgumentException("Can not call getResultList on a modification query!");
    }

    @Override
    public Object getSingleResult() {
        throw new IllegalArgumentException("Can not call getSingleResult on a modification query!");
    }

    @Override
    public int executeUpdate() {
        bindParameters();
        return querySpecification.createModificationPlan(firstResult, maxResults).executeUpdate();
    }

    @Override
    public Query setHint(String hintName, Object value) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<String, Object> getHints() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public Query setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (querySpecification.getParticipatingQueries().size() > 1) {
            throw new PersistenceException("Unsupported unwrap: " + cls.getName());
        }
        return delegate.unwrap(cls);
    }
}
