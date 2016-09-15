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

    void parameterizeQuery(Query q) {
        Set<String> requestedValueParameters = new HashSet<String>();
        for (Parameter<?> p : q.getParameters()) {
            String parameterName = p.getName();
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

            parameter.bind(q);
        }

        for (String parameterName : requestedValueParameters) {
            ParameterImpl<?> parameter = parameters.get(parameterName);
            parameter.bind(q);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Parameter<?> getParameter(String parameterName) {
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
        String name = addParameter(o);
        return new ParameterExpression(name, o, o instanceof Collection);
    }

    public String addParameter(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        String name = prefix + counter++;
        parameters.put(name, new ParameterImpl<Object>(name, o));
        return name;
    }

    public void addParameterMapping(String parameterName, Object o) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, o));
    }

    public void registerParameterName(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (!parameters.containsKey(parameterName)) {
            parameters.put(parameterName, new ParameterImpl<Object>(parameterName));
        }
    }

    public void registerValuesParameter(String parameterName, Class<?> type, String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException("Can't register parameter for VALUES clause because there already exists a parameter with the name: " + parameterName);
        }
        parameters.put(parameterName, new ParameterImpl<Object>(parameterName, new ValuesParameterWrapper(type, parameterNames, pathExpressions)));
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
        private Class<T> parameterType;
        private T value;

        public ParameterImpl(String name) {
            this.name = name;
            this.position = null;
        }

        public ParameterImpl(String name, T value) {
            this.name = name;
            this.position = null;
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

        @Override
        public Class<T> getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class<T> parameterType) {
            this.parameterType = parameterType;
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
        private final String[][] parameterNames;
        private final ValueRetriever<Object, Object>[] pathExpressions;
        private Collection<Object> value;

        public ValuesParameterWrapper(Class<?> type, String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
            this.type = type;
            this.parameterNames = parameterNames;
            this.pathExpressions = pathExpressions;
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

            // NOTE: be careful when changing this, there might be code that depends on this not being copied for performance
            this.value = (Collection<Object>) value;
            return this;
        }

        @Override
        public void bind(Query query, String name) {
            if (value == null) {
                throw new IllegalArgumentException("No values are bound for parameter with name: " + name);
            }

            Iterator<Object> iterator = value.iterator();
            for (int i = 0; i < parameterNames.length; i++) {
                if (iterator.hasNext()) {
                    Object element = iterator.next();
                    for (int j = 0; j < parameterNames[i].length; j++) {
                        query.setParameter(parameterNames[i][j], pathExpressions[j].getValue(element));
                    }
                } else {
                    for (int j = 0; j < parameterNames[i].length; j++) {
                        query.setParameter(parameterNames[i][j], null);
                    }
                }
            }
        }
    }
}
