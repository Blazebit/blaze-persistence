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
import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
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
    private final Set<String> aggregateFunctions;
    private final ExpressionFactory expressionFactory;
    private final ExpressionFactory subqueryExpressionFactory;
    private final Map<String, String> properties;
    private final boolean compatibleModeEnabled;
    
    private final String configuredDbms;
    private final DbmsDialect configuredDbmsDialect;
    private final Set<String> configuredRegisteredFunctions;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config, EntityManagerFactory entityManagerFactory) {
        this.queryTransformers = new ArrayList<QueryTransformer>(config.getQueryTransformers());
        this.extendedQuerySupport = config.getExtendedQuerySupport();
        this.dbmsDialects = new HashMap<String, DbmsDialect>(config.getDbmsDialects());
        this.aggregateFunctions = resolveAggregateFunctions(config.getFunctions());
        this.expressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl(aggregateFunctions));
        this.subqueryExpressionFactory = new SubqueryExpressionFactory(aggregateFunctions, expressionFactory);
        this.properties = copyProperties(config.getProperties());
        this.compatibleModeEnabled = Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.COMPATIBLE_MODE)));
        
        EntityManagerFactory emf = entityManagerFactory;
        Set<String> registeredFunctions = new HashSet<String>();
        String dbms = null;
        for (EntityManagerFactoryIntegrator integrator : config.getEntityManagerIntegrators()) {
            emf = integrator.registerFunctions(emf, config.getFunctions());
            registeredFunctions.addAll(integrator.getRegisteredFunctions(emf));
            dbms = integrator.getDbms(emf);
        }

        DbmsDialect dialect = dbmsDialects.get(dbms);
        
        // Use the default dialect
        if (dialect == null) {
            dialect = dbmsDialects.get(null);
        }
        
        this.configuredDbms = dbms;
        this.configuredDbmsDialect = dialect;
        this.configuredRegisteredFunctions = registeredFunctions;
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

    public ExpressionFactory getSubqueryExpressionFactory() {
        return subqueryExpressionFactory;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public boolean isCompatibleModeEnabled() {
        return compatibleModeEnabled;
    }
    
    private MainQuery createMainQuery(EntityManager entityManager) {
        return MainQuery.create(this, entityManager, configuredDbms, configuredDbmsDialect, configuredRegisteredFunctions);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingSetOperationCriteriaBuilder<T>> startSet(EntityManager entityManager, Class<T> resultClass) {
        MainQuery mainQuery = createMainQuery(entityManager);
        FinalSetOperationCriteriaBuilderImpl<T> parentFinalSetOperationBuilder = new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, true, resultClass, null, false, null);
        OngoingFinalSetOperationCriteriaBuilderImpl<T> subFinalSetOperationBuilder = new OngoingFinalSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultClass, null, true, parentFinalSetOperationBuilder.getSubListener());
        
        LeafOngoingSetOperationCriteriaBuilderImpl<T> leafCb = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultClass, parentFinalSetOperationBuilder.getSubListener(), parentFinalSetOperationBuilder);
        OngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingSetOperationCriteriaBuilder<T>> cb = new OngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingSetOperationCriteriaBuilder<T>>(mainQuery, false, resultClass, subFinalSetOperationBuilder.getSubListener(), subFinalSetOperationBuilder, leafCb);
        
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        subFinalSetOperationBuilder.setEndSetResult((T) leafCb);
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);

        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);
        
        return cb;
    }

    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass) {
        return create(entityManager, resultClass, null);
    }

    @Override
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass, String alias) {
        MainQuery mainQuery = createMainQuery(entityManager);
        CriteriaBuilderImpl<T> cb = new CriteriaBuilderImpl<T>(mainQuery, true, resultClass, alias);
        return cb;
    }

    @Override
	public <T> DeleteCriteriaBuilder<T> delete(EntityManager entityManager, Class<T> deleteClass) {
        return delete(entityManager, deleteClass, null);
	}

	@Override
	public <T> DeleteCriteriaBuilder<T> delete(EntityManager entityManager, Class<T> deleteClass, String alias) {
        MainQuery mainQuery = createMainQuery(entityManager);
        DeleteCriteriaBuilderImpl<T> cb = new DeleteCriteriaBuilderImpl<T>(mainQuery, deleteClass, alias);
        return cb;
	}

    @Override
	public <T> UpdateCriteriaBuilder<T> update(EntityManager entityManager, Class<T> updateClass) {
        return update(entityManager, updateClass, null);
	}

	@Override
	public <T> UpdateCriteriaBuilder<T> update(EntityManager entityManager, Class<T> updateClass, String alias) {
        MainQuery mainQuery = createMainQuery(entityManager);
        UpdateCriteriaBuilderImpl<T> cb = new UpdateCriteriaBuilderImpl<T>(mainQuery, updateClass, alias);
        return cb;
	}

	@Override
	public <T> InsertCriteriaBuilder<T> insert(EntityManager entityManager, Class<T> insertClass) {
        MainQuery mainQuery = createMainQuery(entityManager);
        InsertCriteriaBuilderImpl<T> cb = new InsertCriteriaBuilderImpl<T>(mainQuery, insertClass);
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

    private Map<String, String> copyProperties(Properties properties) {
        Map<String, String> newProperties = new HashMap<String, String>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            newProperties.put(key, value);
        }

        return newProperties;
    }

}
