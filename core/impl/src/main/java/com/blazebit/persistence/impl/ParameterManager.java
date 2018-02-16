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

package com.blazebit.persistence.impl;

import java.util.*;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import com.blazebit.lang.ValueRetriever;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ParameterExpression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ParameterManager {

    private static final String PREFIX = "param_";
    private int counter;
    private final Map<String, ParameterImpl<?>> parameters = new HashMap<String, ParameterImpl<?>>();
    private final Map<String, String> valuesParameters = new HashMap<String, String>();
    private final ParameterRegistrationVisitor parameterRegistrationVisitor;
    private final ParameterUnregistrationVisitor parameterUnregistrationVisitor;

    public ParameterManager() {
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(this);
        this.parameterUnregistrationVisitor = new ParameterUnregistrationVisitor(this);
    }

    public void collectParameterRegistrations(Expression expression, ClauseType clauseType) {
        try {
            parameterRegistrationVisitor.setClauseType(clauseType);
            expression.accept(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(null);
        }
    }

    public void collectParameterUnregistrations(Expression expression, ClauseType clauseType) {
        try {
            parameterUnregistrationVisitor.setClauseType(clauseType);
            expression.accept(parameterUnregistrationVisitor);
        } finally {
            parameterUnregistrationVisitor.setClauseType(null);
        }
    }

    Set<String> getParameterListNames(Query q) {
        return getParameterListNames(q, Collections.EMPTY_SET);
    }

    Set<String> getParameterListNames(Query q, Set<String> skippedParameters) {
        Set<String> parameterListNames = new HashSet<String>();
        collectParameterListNames(q, parameterListNames, skippedParameters);
        return parameterListNames;
    }

    void collectParameterListNames(Query q, Set<String> parameterListNames) {
        collectParameterListNames(q, parameterListNames, Collections.EMPTY_SET);
    }

    void collectParameterListNames(Query q, Set<String> parameterListNames, Set<String> skippedParameters) {
        for (Parameter<?> p: q.getParameters()) {
            String name = p.getName();
            if (skippedParameters.contains(name)) {
                continue;
            }
            if (getParameter(name).isCollectionValued()) {
                parameterListNames.add(name);
            }
        }
    }

    void parameterizeQuery(Query q) {
        parameterizeQuery(q, Collections.EMPTY_SET);
    }

    void parameterizeQuery(Query q, Set<String> skippedParameters) {
        Set<String> requestedValueParameters = new HashSet<String>();
        for (Parameter<?> p : q.getParameters()) {
            String parameterName = p.getName();
            if (skippedParameters.contains(parameterName)) {
                continue;
            }
            ParameterImpl<?> parameter = parameters.get(parameterName);
            if (parameter == null) {
                String valuesParameter = valuesParameters.get(parameterName);
                if (valuesParameter == null) {
                    throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
                }

                // Skip binding the sub-parameter, we will do that in one go at the end
                requestedValueParameters.add(valuesParameter);
                continue;
            }

            // If a query requests the values parameter directly, it is aware of handling it
            if (parameter.getParameterValue() instanceof ValuesParameterWrapper) {
                if (parameter.getValue() != null) {
                    q.setParameter(parameterName, parameter.getValue());
                }
            } else {
                parameter.bind(q);
            }
        }

        for (String parameterName : requestedValueParameters) {
            ParameterImpl<?> parameter = parameters.get(parameterName);
            parameter.bind(q);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ParameterImpl<?> getParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        return parameter;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<? extends Parameter<?>> getParameters() {
        return new HashSet<Parameter<?>>(parameters.values());
    }

    public Map<String, String> getValuesParameters() {
        return Collections.unmodifiableMap(valuesParameters);
    }

    public Map<String, ParameterValueTransformer> getTransformers() {
        Map<String, ParameterValueTransformer> transformers = new HashMap<>();
        for (Map.Entry<String, ParameterImpl<?>> entry : parameters.entrySet()) {
            ParameterValueTransformer transformer = entry.getValue().getTranformer();
            if (transformer != null) {
                transformers.put(entry.getKey(), transformer);
            }
        }
        return transformers;
    }

    public Map<String, ValuesParameterBinder> getValuesBinders() {
        Map<String, ValuesParameterBinder> binders = new HashMap<>();
        for (Map.Entry<String, ParameterImpl<?>> entry : parameters.entrySet()) {
            ParameterValue value = entry.getValue().getParameterValue();
            if (value instanceof ValuesParameterWrapper) {
                binders.put(entry.getKey(), ((ValuesParameterWrapper) value).getBinder());
            }
        }
        return binders;
    }

    public boolean containsParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        return parameters.containsKey(parameterName);
    }

    public boolean isParameterSet(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        return parameter != null && parameter.getValue() != null;
    }

    public Object getParameterValue(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        return parameter.getValue();
    }

    public ParameterExpression addParameterExpression(Object o, ClauseType clause) {
        String name = addParameter(o, o instanceof Collection, clause);
        return new ParameterExpression(name, o, o instanceof Collection);
    }

    private String addParameter(Object o, boolean collectionValued, ClauseType clause) {
        if (o == null) {
            throw new NullPointerException();
        }
        String name = PREFIX + counter++;
        parameters.put(name, new ParameterImpl<Object>(name, collectionValued, clause, o));
        return name;
    }

    public void addParameterMapping(String parameterName, Object o, ClauseType clause) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, o instanceof Collection, clause, o));
    }

    public void registerParameterName(String parameterName, boolean collectionValued, ClauseType clause) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            parameters.put(parameterName, new ParameterImpl<Object>(parameterName, collectionValued, clause));
        } else {
            parameter.getClauseTypes().add(clause);
        }
    }

    public void unregisterParameterName(String parameterName, ClauseType clauseType) {
        ParameterImpl<?> parameter = parameters.get(parameterName);
        parameter.getClauseTypes().remove(clauseType);
        if (parameter.getClauseTypes().isEmpty()) {
            parameters.remove(parameterName);
        }
    }

    public void registerValuesParameter(String parameterName, Class<?> type, String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Can't register parameter for VALUES clause because there already exists a parameter with the name: " + parameterName);
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, false, ClauseType.JOIN, new ValuesParameterWrapper(type, parameterNames, pathExpressions)));
        for (int i = 0; i < parameterNames.length; i++) {
            for (int j = 0; j < parameterNames[i].length; j++) {
                valuesParameters.put(parameterNames[i][j], parameterName);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Object parameterValue) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(parameterValue);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Calendar value, TemporalType temporalType) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(new TemporalCalendarParameterWrapper(temporalType, value));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void satisfyParameter(String parameterName, Date value, TemporalType temporalType) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setValue(new TemporalDateParameterWrapper(temporalType, value));
    }

    @SuppressWarnings({ "unchecked" })
    public void setParameterType(String parameterName, Class<?> type) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        // TODO: maybe we should do some checks here?
        parameter.setParameterType((Class) type);
    }

    // TODO: needs equals-hashCode implementation

    static final class ParameterImpl<T> implements Parameter<T> {

        private final String name;
        private final Integer position;
        private final boolean collectionValued;
        private final Set<ClauseType> clauseTypes;
        private Class<T> parameterType;
        private T value;
        private boolean valueSet;
        private ParameterValueTransformer tranformer;

        public ParameterImpl(String name, boolean collectionValued, ClauseType clause) {
            this.name = name;
            this.position = null;
            this.collectionValued = collectionValued;
            this.clauseTypes = EnumSet.of(clause);
        }

        public ParameterImpl(String name, boolean collectionValued, ClauseType clause, T value) {
            this.name = name;
            this.position = null;
            this.collectionValued = collectionValued;
            this.clauseTypes = EnumSet.of(clause);
            setValue(value);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getPosition() {
            return position;
        }

        public boolean isCollectionValued() {
            return collectionValued;
        }

        public Set<ClauseType> getClauseTypes() {
            return clauseTypes;
        }

        @Override
        public Class<T> getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class<T> parameterType) {
            this.parameterType = parameterType;
        }

        public ParameterValue getParameterValue() {
            if (value instanceof ParameterValue) {
                return (ParameterValue) value;
            }
            return null;
        }

        public boolean isValueSet() {
            return valueSet;
        }

        @SuppressWarnings("unchecked")
        public T getValue() {
            if (value instanceof ParameterValue) {
                return (T) ((ParameterValue) value).getValue();
            }

            return value;
        }

        @SuppressWarnings({ "unchecked" })
        public void setValue(T value) {
            this.valueSet = true;
            if (tranformer != null) {
                value = transform(value);
            }
            if (this.value instanceof ParameterValue) {
                this.value = (T) ((ParameterValue) this.value).withValue(value);
            } else {
                this.value = value;
                if (value != null) {
                    if (value instanceof ParameterValue) {
                        parameterType = (Class<T>) ((ParameterValue) value).getValueType();
                    } else {
                        parameterType = (Class<T>) value.getClass();
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private T transform(T value) {
            if (value instanceof Collection<?>) {
                Collection<?> values = (Collection<?>) value;
                List<Object> list = new ArrayList<>(values.size());
                for (Object o : values) {
                    list.add(tranformer.transform(o));
                }
                return (T) list;
            } else {
                return (T) tranformer.transform(value);
            }
        }

        public ParameterValueTransformer getTranformer() {
            return tranformer;
        }

        public void setTranformer(ParameterValueTransformer tranformer) {
            if (this.tranformer == null) {
                this.tranformer = tranformer;
                if (valueSet) {
                    this.value = transform(value);
                }
            } else if (!this.tranformer.equals(tranformer)) {
                throw new IllegalStateException("Tried to set parameter value transformer [" + tranformer + "] although a transformer [" + this.tranformer + "] is already set for parameter: " + name);
            }
        }

        public void bind(Query q) {
            if (valueSet) {
                if (value instanceof ParameterValue) {
                    ((ParameterValue) value).bind(q, name);
                } else {
                    q.setParameter(name, value);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Parameter<?>)) {
                return false;
            }

            Parameter<?> parameter = (Parameter<?>) o;

            if (name != null ? !name.equals(parameter.getName()) : parameter.getName() != null) {
                return false;
            }
            return position != null ? position.equals(parameter.getPosition()) : parameter.getPosition() == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (position != null ? position.hashCode() : 0);
            return result;
        }
    }

    static interface ParameterValue {

        public Class<?> getValueType();

        public Object getValue();

        public ParameterValue withValue(Object value);

        public void bind(Query query, String name);

    }

    static final class TemporalCalendarParameterWrapper implements ParameterValue {

        private final TemporalType type;
        private Calendar value;

        public TemporalCalendarParameterWrapper(TemporalType type, Calendar value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public Calendar getValue() {
            return value;
        }

        @Override
        public ParameterValue withValue(Object value) {
            this.value = (Calendar) value;
            return this;
        }

        @Override
        public Class<?> getValueType() {
            return Calendar.class;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, type);
        }
    }

    static final class TemporalDateParameterWrapper implements ParameterValue {

        private final TemporalType type;
        private Date value;

        public TemporalDateParameterWrapper(TemporalType type, Date value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public Date getValue() {
            return value;
        }

        @Override
        public ParameterValue withValue(Object value) {
            this.value = (Date) value;
            return this;
        }

        @Override
        public Class<?> getValueType() {
            return Date.class;
        }

        @Override
        public void bind(Query query, String name) {
            query.setParameter(name, value, type);
        }
    }

    static final class ValuesParameterWrapper implements ParameterValue {

        private final Class<?> type;
        private final ValuesParameterBinder binder;
        private Collection<Object> value;

        public ValuesParameterWrapper(Class<?> type, String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
            this.type = type;
            this.binder = new ValuesParameterBinder(parameterNames, pathExpressions);
        }

        public ValuesParameterBinder getBinder() {
            return binder;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Class<?> getValueType() {
            return Collection.class;
        }

        @Override
        public ParameterValue withValue(Object value) {
            if (value == null) {
                throw new IllegalArgumentException("null not allowed for VALUES parameter!");
            }
            if (!(value instanceof Collection<?>)) {
                throw new IllegalArgumentException("Value for VALUES parameter must be a collection! Unsupported type: " + value.getClass());
            }

            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) value;
            if (collection.size() > binder.size()) {
                throw new IllegalArgumentException("The size of the collection must be lower or equal to the specified size for the VALUES clause.");
            }
            // NOTE: be careful when changing this, there might be code that depends on this not being copied for performance
            this.value = collection;
            return this;
        }

        @Override
        public void bind(Query query, String name) {
            if (value == null) {
                throw new IllegalArgumentException("No values are bound for parameter with name: " + name);
            }

            binder.bind(query, value);
        }
    }
}
