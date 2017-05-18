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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.TemporalType;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.JpqlMacro;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CommonQueryBuilderAdapter<BuilderType extends CommonQueryBuilder<BuilderType>> implements CommonQueryBuilder<BuilderType> {

    private final AbstractCommonQueryBuilder<?, BuilderType, ?, ?, ?> builder;

    @SuppressWarnings("unchecked")
    public CommonQueryBuilderAdapter(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        this.builder = (AbstractCommonQueryBuilder<?, BuilderType, ?, ?, ?>) builder;
    }
    
    @Override
    public EntityMetamodel getMetamodel() {
        return builder.getMetamodel();
    }

    @Override
    public CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return builder.getCriteriaBuilderFactory();
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return builder.getService(serviceClass);
    }

    @Override
    public BuilderType setParameter(String name, Object value) {
        return builder.setParameter(name, value);
    }

    @Override
    public BuilderType setParameter(String name, Calendar value, TemporalType temporalType) {
        return builder.setParameter(name, value, temporalType);
    }

    @Override
    public BuilderType setParameter(String name, Date value, TemporalType temporalType) {
        return builder.setParameter(name, value, temporalType);
    }

    @Override
    public BuilderType setParameterType(String name, Class<?> type) {
        return builder.setParameterType(name, type);
    }

    @Override
    public boolean containsParameter(String name) {
        return builder.containsParameter(name);
    }

    @Override
    public boolean isParameterSet(String name) {
        return builder.isParameterSet(name);
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return builder.getParameter(name);
    }

    @Override
    public Set<? extends Parameter<?>> getParameters() {
        return builder.getParameters();
    }

    @Override
    public Object getParameterValue(String name) {
        return builder.getParameterValue(name);
    }

    @Override
    public BuilderType registerMacro(String macroName, JpqlMacro jpqlMacro) {
        return builder.registerMacro(macroName, jpqlMacro);
    }

    @Override
    public BuilderType setProperty(String propertyName, String propertyValue) {
        return builder.setProperty(propertyName, propertyValue);
    }

    @Override
    public BuilderType setProperties(Map<String, String> properties) {
        return builder.setProperties(properties);
    }

    @Override
    public Map<String, String> getProperties() {
        return builder.getProperties();
    }

    @Override
    public String getProperty(String propertyName) {
        return builder.getProperty(propertyName);
    }

    @Override
    public BuilderType setCacheable(boolean cacheable) {
        return builder.setCacheable(cacheable);
    }

    @Override
    public boolean isCacheable() {
        return builder.isCacheable();
    }
}
