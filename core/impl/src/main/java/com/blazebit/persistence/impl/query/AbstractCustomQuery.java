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
import com.blazebit.persistence.impl.util.SetView;
import com.blazebit.persistence.spi.CteQueryWrapper;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCustomQuery<T> implements Query, CteQueryWrapper {

    protected final QuerySpecification<T> querySpecification;
    protected final Map<String, ParameterValueTransformer> transformers;
    protected final Map<String, ValuesParameter> valuesParameters;
    protected final Map<String, String> valuesElementParameters;
    protected final Map<String, Parameter<?>> parameters;
    protected final Map<String, ValueBinder> valueBinders;
    protected int firstResult;
    protected int maxResults = Integer.MAX_VALUE;

    public AbstractCustomQuery(QuerySpecification<T> querySpecification, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        this.querySpecification = querySpecification;
        Map<String, ValuesParameter> valuesParameterMap = new HashMap<String, ValuesParameter>();
        Map<String, Parameter<?>> parameters = new HashMap<>();
        this.valueBinders = new HashMap<>(parameters.size());

        for (Parameter<?> p : querySpecification.getParameters()) {
            String name = p.getName();
            ValuesParameterBinder valuesParameterBinder = valuesBinders.get(name);
            if (valuesParameterBinder == null) {
                parameters.put(name, p);
                valueBinders.put(name, null);
            } else {
                ValuesParameter param = new ValuesParameter(name, valuesParameterBinder);
                parameters.put(name, param);
                valueBinders.put(name, null);
                valuesParameterMap.put(name, param);
            }
        }
        this.transformers = Collections.unmodifiableMap(transformers);
        this.valuesParameters = Collections.unmodifiableMap(valuesParameterMap);
        this.valuesElementParameters = Collections.unmodifiableMap(valuesParameters);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public QuerySpecification<T> getQuerySpecification() {
        return querySpecification;
    }

    public String getSql() {
        return querySpecification.getSql();
    }

    @Override
    public List<Query> getParticipatingQueries() {
        return querySpecification.getParticipatingQueries();
    }

    @Override
    public Query setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public Query setFirstResult(int startPosition) {
        this.firstResult = startPosition;
        return this;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    protected void bindParameters() {
        Set<String> missingParameters = null;
        for (Query q : querySpecification.getParticipatingQueries()) {
            for (Parameter<?> p : q.getParameters()) {
                String name = p.getName();
                String valuesName = valuesElementParameters.get(name);
                if (valuesName == null) {
                    ValueBinder valueBinder = valueBinders.get(name);
                    if (valueBinder == null) {
                        if (missingParameters == null) {
                            missingParameters = new HashSet<>();
                        }
                        missingParameters.add(name);
                    } else {
                        valueBinder.bind(q, name);
                    }
                } else {
                    ValuesParameter valuesParameter = valuesParameters.get(valuesName);
                    if (valuesParameter.getValue() == null) {
                        if (missingParameters == null) {
                            missingParameters = new HashSet<>();
                        }
                        missingParameters.add(name);
                    } else {
                        valuesParameter.bind(q);
                    }
                }
            }
        }
        if (missingParameters != null && !missingParameters.isEmpty()) {
            throw new IllegalArgumentException("The following parameters have not been set: " + missingParameters);
        }
    }

    @Override
    public <T> Query setParameter(Parameter<T> param, T value) {
        setParameter(param.getName(), value);
        return this;
    }

    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        setParameter(param.getName(), value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        setParameter(param.getName(), value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(String name, Object value) {
        ValuesParameter valuesParameter = valuesParameters.get(name);
        if (valuesParameter == null) {
            if (!parameters.containsKey(name)) {
                throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
            }
            ParameterValueTransformer transformer = transformers.get(name);
            if (transformer != null) {
                value = transformer.transform(value);
            }
            if (value instanceof Collection<?>) {
                querySpecification.onCollectionParameterChange(name, (Collection<?>) value);
            }
            valueBinders.put(name, new DefaultValueBinder(value));
        } else {
            valuesParameter.setValue(value);
        }

        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        if (!parameters.containsKey(name)) {
            throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
        }
        valueBinders.put(name, new CalendarValueBinder(value, temporalType));

        return this;
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        if (!parameters.containsKey(name)) {
            throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
        }
        valueBinders.put(name, new DateValueBinder(value, temporalType));

        return this;
    }

    @Override
    public Query setParameter(int position, Object value) {
        return setParameter(Integer.toString(position), value);
    }

    @Override
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        return setParameter(Integer.toString(position), value, temporalType);
    }

    @Override
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        return setParameter(Integer.toString(position), value, temporalType);
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return new SetView<>(parameters.values());
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        Parameter<?> p = getParameter(name);
        if (!type.isAssignableFrom(p.getParameterType())) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not assignable to '" + type.getName() + "'!");
        }
        return (Parameter<T>) p;
    }

    @Override
    public Parameter<?> getParameter(int position) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        ValuesParameter valuesParameter = valuesParameters.get(param.getName());
        if (valuesParameter != null) {
            return valuesParameter.getValue() != null;
        }

        return valueBinders.get(param.getName()) != null;
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return (T) getParameterValue(param.getName());
    }

    @Override
    public Object getParameterValue(String name) {
        ValuesParameter valuesParameter = valuesParameters.get(name);
        if (valuesParameter != null) {
            return valuesParameter.getValue();
        }

        ValueBinder valueBinder = valueBinders.get(name);
        return valueBinder == null ? null : valueBinder.getValue();
    }

    @Override
    public Object getParameterValue(int position) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static interface ValueBinder {
        void bind(Query query, String name);
        Object getValue();
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class DefaultValueBinder implements ValueBinder {
        private final Object value;

        public DefaultValueBinder(Object value) {
            this.value = value;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value);
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class CalendarValueBinder implements ValueBinder {
        private final Calendar value;
        private final TemporalType temporalType;

        public CalendarValueBinder(Calendar value, TemporalType temporalType) {
            this.value = value;
            this.temporalType = temporalType;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, temporalType);
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class DateValueBinder implements ValueBinder {
        private final Date value;
        private final TemporalType temporalType;

        public DateValueBinder(Date value, TemporalType temporalType) {
            this.value = value;
            this.temporalType = temporalType;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, temporalType);
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class ValuesParameter implements Parameter<Collection> {

        private final String name;
        private final ValuesParameterBinder binder;
        private Collection<Object> value;

        public ValuesParameter(String name, ValuesParameterBinder binder) {
            this.name = name;
            this.binder = binder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getPosition() {
            return null;
        }

        @Override
        public Class<Collection> getParameterType() {
            return Collection.class;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            if (value == null) {
                throw new IllegalArgumentException("null not allowed for VALUES parameter!");
            }
            if (!(value instanceof Collection<?>)) {
                throw new IllegalArgumentException("Value for VALUES parameter must be a collection! Unsupported type: " + value.getClass());
            }

            Collection<Object> collection = (Collection<Object>) value;
            if (collection.size() > binder.size()) {
                throw new IllegalArgumentException("The size of the collection must be lower or equal to the specified size for the VALUES clause.");
            }
            this.value = collection;
        }

        public void bind(Query query) {
            binder.bind(query, value);
        }
    }
}
