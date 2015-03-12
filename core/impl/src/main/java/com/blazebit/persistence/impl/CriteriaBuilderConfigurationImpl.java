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

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.function.pageposition.MySQLPagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.OraclePagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.TransactSQLPagePositionFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.QueryTransformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderConfigurationImpl implements CriteriaBuilderConfiguration {

    private final List<QueryTransformer> queryTransformers = new ArrayList<QueryTransformer>();
    private final Map<String, Map<String, JpqlFunction>> functions = new HashMap<String, Map<String, JpqlFunction>>();
    private final List<EntityManagerIntegrator> entityManagerEnrichers = new ArrayList<EntityManagerIntegrator>();
    private Properties properties = new Properties();

    public CriteriaBuilderConfigurationImpl() {
        loadDefaultProperties();
        loadQueryTransformers();
        loadEntityManagerIntegrator();
        loadFunctions();
    }
    
    private void loadFunctions() {
        Map<String, JpqlFunction> jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new PagePositionFunction());
        jpqlFunctions.put("mysql", new MySQLPagePositionFunction());
        jpqlFunctions.put("oracle", new OraclePagePositionFunction());
        jpqlFunctions.put("sybase", new TransactSQLPagePositionFunction());
        jpqlFunctions.put("microsoft", new TransactSQLPagePositionFunction());
        functions.put("page_position", jpqlFunctions);
    }

    private void loadDefaultProperties() {
    }

    private void loadQueryTransformers() {
        ServiceLoader<QueryTransformer> serviceLoader = ServiceLoader.load(QueryTransformer.class);
        Iterator<QueryTransformer> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            QueryTransformer transformer = iterator.next();
            queryTransformers.add(transformer);
        }
    }

    private void loadEntityManagerIntegrator() {
        ServiceLoader<EntityManagerIntegrator> serviceLoader = ServiceLoader.load(EntityManagerIntegrator.class);
        Iterator<EntityManagerIntegrator> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            EntityManagerIntegrator enricher = iterator.next();
            entityManagerEnrichers.add(enricher);
        }
    }
    
    @Override
    public CriteriaBuilderConfiguration registerFunction(String name, JpqlFunction function) {
        return registerFunction(name, null, function);
    }
    
    @Override
    public CriteriaBuilderConfiguration registerFunction(String name, String dbms, JpqlFunction function) {
        String functionName = name.toLowerCase();
        Map<String, JpqlFunction> dbmsFunctions = functions.get(functionName);
        
        if (dbmsFunctions == null) {
            functions.put(functionName, dbmsFunctions = new HashMap<String, JpqlFunction>());
        }
        
        dbmsFunctions.put(dbms == null ? null : dbms.toLowerCase(), function);
        return this;
    }
    
    public Map<String, Map<String, JpqlFunction>> getFunctions() {
        return functions;
    }
    
    @Override
    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    @Override
    public CriteriaBuilderConfiguration registerQueryTransformer(QueryTransformer transformer) {
        queryTransformers.add(transformer);
        return this;
    }

    @Override
    public List<QueryTransformer> getQueryTransformers() {
        return queryTransformers;
    }

    @Override
    public CriteriaBuilderConfiguration registerEntityManagerIntegrator(EntityManagerIntegrator entityManagerEnricher) {
        entityManagerEnrichers.add(entityManagerEnricher);
        return this;
    }

    @Override
    public List<EntityManagerIntegrator> getEntityManagerIntegrators() {
        return entityManagerEnrichers;
    }

    @Override
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        return new CriteriaBuilderFactoryImpl(this);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public CriteriaBuilderConfiguration setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration addProperties(Properties extraProperties) {
        this.properties.putAll(extraProperties);
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration mergeProperties(Properties properties) {
        for (Map.Entry entry : properties.entrySet()) {
            if (this.properties.containsKey(entry.getKey())) {
                continue;
            }
            this.properties.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration setProperty(String propertyName, String value) {
        properties.setProperty(propertyName, value);
        return this;
    }
}
