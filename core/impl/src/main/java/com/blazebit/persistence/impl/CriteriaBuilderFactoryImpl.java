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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.spi.QueryTransformer;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final List<QueryTransformer> queryTransformers;
    private final ExtendedQuerySupport extendedQuerySupport;
    private final Map<String, DbmsDialect> dbmsDialects;
    private final Map<String, JpqlFunctionGroup> functions;
    private final Set<String> aggregateFunctions;
    private final List<EntityManagerIntegrator> entityManagerIntegrators;
    private final ExpressionFactory expressionFactory;
    private final Map<String, Object> properties;
    private final boolean compatibleModeEnabled;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config) {
        this.queryTransformers = new ArrayList<QueryTransformer>(config.getQueryTransformers());
        this.extendedQuerySupport = config.getExtendedQuerySupport();
        this.dbmsDialects = new HashMap<String, DbmsDialect>(config.getDbmsDialects());
        this.functions = new HashMap<String, JpqlFunctionGroup>(config.getFunctions());
        this.aggregateFunctions = resolveAggregateFunctions(functions);
        this.entityManagerIntegrators = new ArrayList<EntityManagerIntegrator>(config.getEntityManagerIntegrators());
        this.expressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl(aggregateFunctions));
        this.properties = copyProperties(config.getProperties());
        this.compatibleModeEnabled = Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.COMPATIBLE_MODE)));
    }

    private static Set<String> resolveAggregateFunctions(Map<String, JpqlFunctionGroup> functions) {
        Set<String> aggregateFunctions = new HashSet<String>();
        for (Map.Entry<String, JpqlFunctionGroup> entry : functions.entrySet()) {
            if (entry.getValue().isAggregate()) {
                aggregateFunctions.add(entry.getKey().toLowerCase());
            }
        }
        return aggregateFunctions;
    }

    public List<QueryTransformer> getQueryTransformers() {
        return queryTransformers;
    }

    public ExtendedQuerySupport getExtendedQuerySupport() {
        return extendedQuerySupport;
    }

    public Set<String> getAggregateFunctions() {
        return aggregateFunctions;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean isCompatibleModeEnabled() {
        return compatibleModeEnabled;
    }

    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass) {
        return create(entityManager, resultClass, null);
    }

    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass, String alias) {
        Set<String> registeredFunctions = new HashSet<String>();
        EntityManager em = entityManager;
        String dbms = null;
        for (int i = 0; i < entityManagerIntegrators.size(); i++) {
            EntityManagerIntegrator integrator = entityManagerIntegrators.get(i);
            em = integrator.registerFunctions(em, functions);
            registeredFunctions.addAll(integrator.getRegisteredFunctions(em));
            dbms = integrator.getDbms(em);
        }

        DbmsDialect dialect = dbmsDialects.get(dbms);
        
        // Use the default dialect
        if (dialect == null) {
            dialect = dbmsDialects.get(null);
        }
        
        CriteriaBuilderImpl<T> cb = new CriteriaBuilderImpl<T>(this, em, dialect, resultClass, alias, registeredFunctions);
        return cb;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (ExpressionFactory.class.isAssignableFrom(serviceClass)) {
            return (T) expressionFactory;
        }

        return null;
    }

    private Map<String, Object> copyProperties(Properties properties) {
        Map<String, Object> newProperties = new HashMap<String, Object>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            newProperties.put(key, value);
        }

        return newProperties;
    }

}
