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
import javax.persistence.TemporalType;

import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ParameterManager {

    private static final String prefix = "param_";
    private int counter;
    private final Map<String, ParameterImpl<?>> parameters = new HashMap<String, ParameterImpl<?>>();
    private final VisitorAdapter parameterRegistrationVisitor;

    public ParameterManager() {
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(this);
    }

    public VisitorAdapter getParameterRegistrationVisitor() {
        return parameterRegistrationVisitor;
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
        return new ParameterExpression(name, o);
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

    @SuppressWarnings({ "unchecked" })
    public void setParameterType(String parameterName, Class<?> type) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        ParameterImpl<?> parameter = parameters.get(parameterName);
        if (parameter == null) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameter.setParameterType((Class) type);
    }

    // TODO: needs equals-hashCode implementation

    private static class ParameterImpl<T> implements Parameter<T> {

        private final String name;
        private Class<T> parameterType;
        private T value;

        public ParameterImpl(String name) {
            this.name = name;
        }

        public ParameterImpl(String name, T value) {
            this.name = name;
            setValue(value);
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
        public Class<T> getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class<T> parameterType) {
            this.parameterType = parameterType;
        }

        public T getValue() {
            return value;
        }

        @SuppressWarnings({ "unchecked" })
        public void setValue(T value) {
            this.value = value;
            if (value != null) {
                if (value instanceof TemporalCalendarParameterWrapper) {
                    parameterType = (Class<T>) Calendar.class;
                } else if (value instanceof TemporalDateParameterWrapper) {
                    parameterType = (Class<T>) Date.class;
                } else {
                    parameterType = (Class<T>) value.getClass();
                }
            }
        }
    }

    static class TemporalCalendarParameterWrapper<T> {

        private final Calendar value;
        private final TemporalType type;

        public TemporalCalendarParameterWrapper(Calendar value, TemporalType type) {
            this.value = value;
            this.type = type;
        }

        public Calendar getValue() {
            return value;
        }

        public TemporalType getType() {
            return type;
        }
    }

    static class TemporalDateParameterWrapper {

        private final Date value;
        private final TemporalType type;

        public TemporalDateParameterWrapper(Date value, TemporalType type) {
            this.value = value;
            this.type = type;
        }

        public Date getValue() {
            return value;
        }

        public TemporalType getType() {
            return type;
        }
    }
}
