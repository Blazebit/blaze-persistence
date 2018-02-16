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
import com.blazebit.persistence.spi.CteQueryWrapper;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.*;

public abstract class AbstractCustomQuery<T> implements Query, CteQueryWrapper {

    protected final QuerySpecification<T> querySpecification;
    protected final Map<String, ParameterValueTransformer> transformers;
    protected final Map<String, ValuesParameter> valuesParameters;
    protected final Map<String, Set<Query>> parameterQueries;
    protected final Set<Parameter<?>> parameters;
    protected final Set<String> parametersToSet;
    protected int firstResult;
    protected int maxResults = Integer.MAX_VALUE;

    public AbstractCustomQuery(QuerySpecification<T> querySpecification, Map<String, ParameterValueTransformer> transformers, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        this.querySpecification = querySpecification;
        Map<String, ValuesParameter> valuesParameterMap = new HashMap<String, ValuesParameter>();
        Map<String, Set<Query>> parameterQueries = new HashMap<String, Set<Query>>();
        Set<Parameter<?>> parameters = new HashSet<Parameter<?>>();
        Set<String> parametersToSet = new HashSet<String>();
        // TODO: Fix this, currently this builds the complete query plan
        for (Query q : querySpecification.getParticipatingQueries()) {
            for (Parameter<?> p : q.getParameters()) {
                String name = p.getName();
                String valuesName = valuesParameters.get(name);
                if (valuesName != null) {
                    // Replace the name with the values parameter name so the query gets registered for that
                    name = valuesName;
                    if (!valuesParameterMap.containsKey(valuesName)) {
                        ValuesParameter param = new ValuesParameter(valuesName, valuesBinders.get(valuesName));
                        parameters.add(param);
                        valuesParameterMap.put(valuesName, param);

                        if (!q.isBound(p)) {
                            parametersToSet.add(valuesName);
                        }
                    }
                }

                Set<Query> queries = parameterQueries.get(name);
                if (queries == null) {
                    queries = new HashSet<Query>();
                    parameterQueries.put(name, queries);
                    if (valuesName == null) {
                        parameters.add(p);
                        if (!q.isBound(p)) {
                            parametersToSet.add(name);
                        }
                    }
                }
                queries.add(q);
            }
        }
        this.transformers = Collections.unmodifiableMap(transformers);
        this.valuesParameters = Collections.unmodifiableMap(valuesParameterMap);
        this.parameters = Collections.unmodifiableSet(parameters);
        this.parameterQueries = Collections.unmodifiableMap(parameterQueries);
        this.parametersToSet = parametersToSet;
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

    private Set<Query> queries(String name) {
        Set<Query> queries = parameterQueries.get(name);
        if (queries == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' does not exist!");
        }
        return queries;
    }

    protected void validateParameterBindings() {
        if (parametersToSet.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException("The following parameters have not been set: " + parametersToSet);
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
        Set<Query> queries = queries(name);
        ValuesParameter valuesParameter = valuesParameters.get(name);
        if (valuesParameter != null) {
            parametersToSet.remove(name);
            for (Query q : queries) {
                valuesParameter.setValue(value);
                valuesParameter.bind(q);
            }
        } else if (queries.size() > 0) {
            querySpecification.onParameterChange(name);
            parametersToSet.remove(name);
            ParameterValueTransformer transformer = transformers.get(name);
            if (transformer != null) {
                value = transformer.transform(value);
            }

            for (Query q : queries) {
                q.setParameter(name, value);
            }
        } else {
            throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
        }

        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        Set<Query> queries = queries(name);
        if (queries.size() > 0) {
            querySpecification.onParameterChange(name);
            parametersToSet.remove(name);
            for (Query q : queries) {
                q.setParameter(name, value, temporalType);
            }
        } else {
            throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
        }

        return this;
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        Set<Query> queries = queries(name);
        if (queries.size() > 0) {
            querySpecification.onParameterChange(name);
            parametersToSet.remove(name);
            for (Query q : queries) {
                q.setParameter(name, value, temporalType);
            }
        } else {
            throw new IllegalArgumentException("Invalid or unknown parameter with name: " + name);
        }

        return this;
    }

    @Override
    public Query setParameter(int position, Object value) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return parameters;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        ValuesParameter valuesParameter = valuesParameters.get(name);
        if (valuesParameter != null) {
            return valuesParameter;
        }

        Set<Query> queries = queries(name);
        Query q = queries.iterator().next();
        return q.getParameter(name);
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

        Set<Query> queries = queries(param.getName());
        Query q = queries.iterator().next();
        return q.isBound(q.getParameter(param.getName()));
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

        Set<Query> queries = queries(name);
        return queries.iterator().next().getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        throw new IllegalArgumentException("Positional parameters unsupported!");
    }

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
