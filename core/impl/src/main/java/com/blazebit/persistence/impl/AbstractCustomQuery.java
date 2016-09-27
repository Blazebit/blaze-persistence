package com.blazebit.persistence.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.spi.CteQueryWrapper;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.*;

public abstract class AbstractCustomQuery implements Query, CteQueryWrapper {

    protected final List<Query> participatingQueries;
    protected final CommonQueryBuilder<?> cqb;
    protected final ExtendedQuerySupport extendedQuerySupport;
    protected final Map<String, ValuesParameter> valuesParameters;
    protected final Map<String, Set<Query>> parameterQueries;
    protected final Set<Parameter<?>> parameters;
    protected final String sql;
    protected int firstResult;
    protected int maxResults = Integer.MAX_VALUE;

    public AbstractCustomQuery(List<Query> participatingQueries, CommonQueryBuilder<?> cqb, ExtendedQuerySupport extendedQuerySupport, String sql, Map<String, String> valuesParameters, Map<String, ValuesParameterBinder> valuesBinders) {
        this.participatingQueries = participatingQueries;
        this.cqb = cqb;
        this.extendedQuerySupport = extendedQuerySupport;
        this.sql = sql;
        Map<String, ValuesParameter> valuesParameterMap = new HashMap<String, ValuesParameter>();
        Map<String, Set<Query>> parameterQueries = new HashMap<String, Set<Query>>();
        Set<Parameter<?>> parameters = new HashSet<Parameter<?>>();
        for (Query q : participatingQueries) {
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
                    }
                }

                Set<Query> queries = parameterQueries.get(name);
                if (queries == null) {
                    queries = new HashSet<Query>();
                    parameterQueries.put(name, queries);
                    if (valuesName == null) {
                        parameters.add(p);
                    }
                }
                queries.add(q);
            }
        }
        this.valuesParameters = Collections.unmodifiableMap(valuesParameterMap);
        this.parameters = Collections.unmodifiableSet(parameters);
        this.parameterQueries = Collections.unmodifiableMap(parameterQueries);
    }

    public String getSql() {
        return sql;
    }

    @Override
    public List<Query> getParticipatingQueries() {
        return participatingQueries;
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
            for (Query q : queries) {
                valuesParameter.setValue(value);
                valuesParameter.bind(q);
            }
        } else {
            for (Query q : queries) {
                q.setParameter(name, value);
            }
        }
        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        Set<Query> queries = queries(name);
        for (Query q : queries) {
            q.setParameter(name, value, temporalType);
        }
        return this;
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        Set<Query> queries = queries(name);
        for (Query q : queries) {
            q.setParameter(name, value, temporalType);
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
