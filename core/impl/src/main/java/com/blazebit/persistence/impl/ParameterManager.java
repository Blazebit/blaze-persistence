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

import java.util.*;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;

import com.blazebit.lang.ValueRetriever;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.reflection.PropertyPathExpression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ParameterManager {

    private static final String prefix = "param_";
    private int counter;
    private final Map<String, ParameterImpl<?>> parameters = new HashMap<String, ParameterImpl<?>>();
    private final Map<String, String> valuesParameters = new HashMap<String, String>();
    private final VisitorAdapter parameterRegistrationVisitor;

    public ParameterManager() {
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(this);
    }

    public VisitorAdapter getParameterRegistrationVisitor() {
        return parameterRegistrationVisitor;
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
            if (parameter.getParamerterValue() instanceof ValuesParameterWrapper) {
                q.setParameter(parameterName, parameter.getValue());
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

    public Map<String, ValuesParameterBinder> getValuesBinders() {
        Map<String, ValuesParameterBinder> binders = new HashMap<String, ValuesParameterBinder>();
        for (Map.Entry<String, ParameterImpl<?>> entry : parameters.entrySet()) {
            ParamerterValue value = entry.getValue().getParamerterValue();
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

    public ParameterExpression addParameterExpression(Object o) {
        String name = addParameter(o, o instanceof Collection);
        return new ParameterExpression(name, o, o instanceof Collection);
    }

    public String addParameter(Object o, boolean collectionValued) {
        if (o == null) {
            throw new NullPointerException();
        }
        String name = prefix + counter++;
        parameters.put(name, new ParameterImpl<Object>(name, collectionValued, o));
        return name;
    }

    public void addParameterMapping(String parameterName, Object o) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, o instanceof Collection, o));
    }

    public void registerParameterName(String parameterName, boolean collectionValued) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (!parameters.containsKey(parameterName)) {
            parameters.put(parameterName, new ParameterImpl<Object>(parameterName, collectionValued));
        }
    }

    public void registerValuesParameter(String parameterName, Class<?> type, String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Can't register parameter for VALUES clause because there already exists a parameter with the name: " + parameterName);
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, false, new ValuesParameterWrapper(type, parameterNames, pathExpressions)));
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
        private Class<T> parameterType;
        private T value;

        public ParameterImpl(String name, boolean collectionValued) {
            this.name = name;
            this.position = null;
            this.collectionValued = collectionValued;
        }

        public ParameterImpl(String name, boolean collectionValued, T value) {
            this.name = name;
            this.position = null;
            this.collectionValued = collectionValued;
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

        @Override
        public Class<T> getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class<T> parameterType) {
            this.parameterType = parameterType;
        }

        public ParamerterValue getParamerterValue() {
            if (value instanceof ParamerterValue) {
                return (ParamerterValue) value;
            }
            return null;
        }

        public T getValue() {
            if (value instanceof ParamerterValue) {
                return (T) ((ParamerterValue) value).getValue();
            }

            return value;
        }

        @SuppressWarnings({ "unchecked" })
        public void setValue(T value) {
            if (this.value instanceof ParamerterValue) {
                this.value = (T) ((ParamerterValue) this.value).withValue(value);
            } else {
                this.value = value;
                if (value != null) {
                    if (value instanceof ParamerterValue) {
                        parameterType = (Class<T>) ((ParamerterValue) value).getValueType();
                    } else {
                        parameterType = (Class<T>) value.getClass();
                    }
                }
            }
        }

        public void bind(Query q) {
            if (value instanceof ParamerterValue) {
                ((ParamerterValue) value).bind(q, name);
            } else {
                q.setParameter(name, value);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Parameter<?>)) return false;

            Parameter<?> parameter = (Parameter<?>) o;

            if (name != null ? !name.equals(parameter.getName()) : parameter.getName() != null) return false;
            return position != null ? position.equals(parameter.getPosition()) : parameter.getPosition() == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (position != null ? position.hashCode() : 0);
            return result;
        }
    }

    static interface ParamerterValue {

        public Class<?> getValueType();

        public Object getValue();

        public ParamerterValue withValue(Object value);

        public void bind(Query query, String name);

    }

    static final class TemporalCalendarParameterWrapper implements ParamerterValue {

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
        public ParamerterValue withValue(Object value) {
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

    static final class TemporalDateParameterWrapper implements ParamerterValue {

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
        public ParamerterValue withValue(Object value) {
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

    static final class ValuesParameterWrapper implements ParamerterValue {

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
        public ParamerterValue withValue(Object value) {
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
