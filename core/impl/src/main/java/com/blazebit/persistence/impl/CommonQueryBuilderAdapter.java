/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.ParameterExpression;

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
    public BuilderType registerCriteriaParameter(String name, ParameterExpression<?> parameter) {
        return builder.registerCriteriaParameter(name, parameter);
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
