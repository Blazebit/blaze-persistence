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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ParameterManager {

    private static final String prefix = "param_";
    private int counter;
    private final Map<Object, String> nameCache = new IdentityHashMap<Object, String>();
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    private static final Object REGISTERED_PLACEHOLDER = new Object();

    Parameter<?> getParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (!containsParameter(parameterName)) {
            return null;
        }

        Object value = getParameterValue(parameterName);

        return new ParameterImpl(value == null ? null : value.getClass(), parameterName);
    }

    Set<? extends Parameter<?>> getParameters() {
        Set<Parameter<?>> result = new HashSet<Parameter<?>>();

        for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
            Class<?> paramClass = paramEntry.getValue() == null || paramEntry.getValue() == REGISTERED_PLACEHOLDER ? null : paramEntry.getValue().getClass();
            result.add(new ParameterImpl(paramClass, paramEntry.getKey()));
        }
        return result;
    }

    boolean containsParameter(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        return parameters.containsKey(parameterName);
    }

    boolean isParameterSet(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        return parameters.containsKey(parameterName) && parameters.get(parameterName) != REGISTERED_PLACEHOLDER;
    }

    Object getParameterValue(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        Object o = parameters.get(parameterName);
        return o == REGISTERED_PLACEHOLDER ? null : o;
    }

    String getParamNameForObject(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        String existingName = nameCache.get(o);
        if (existingName == null) {
            existingName = prefix + counter++;
            nameCache.put(o, existingName);
            parameters.put(existingName, o);
        }
        return existingName;
    }

    void addParameterMapping(String parameterName, Object o) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        parameters.put(parameterName, o);
    }

    void registerParameterName(String parameterName) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (!parameters.containsKey(parameterName)) {
            parameters.put(parameterName, REGISTERED_PLACEHOLDER);
        }
    }

    void satisfyParameter(String parameterName, Object parameterValue) {
        if (parameterName == null) {
            throw new NullPointerException("parameterName");
        }
        if (!parameters.containsKey(parameterName)) {
            throw new IllegalArgumentException(String.format("Parameter name \"%s\" does not exist", parameterName));
        }
        parameters.put(parameterName, parameterValue);
    }
    
    // TODO: needs equals-hashCode implementation

    private class ParameterImpl<T> implements Parameter<T> {

        private final Class<T> paramClass;
        private final String paramName;

        public ParameterImpl(Class<T> paramClass, String paramName) {
            this.paramClass = paramClass;
            this.paramName = paramName;
        }

        @Override
        public String getName() {
            return paramName;
        }

        @Override
        public Integer getPosition() {
            return null;
        }

        @Override
        public Class<T> getParameterType() {
            return paramClass;
        }

    }

    static class TemporalCalendarParameterWrapper {

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
