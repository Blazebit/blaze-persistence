/*
 * Copyright 2014 - 2016 Blazebit.
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.impl.expression.ExpressionCache;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.impl.util.PropertyUtils;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.spi.QueryTransformer;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final boolean compatibleMode;
    private final boolean optimize;

    private final EntityMetamodel metamodel;
    private final List<QueryTransformer> queryTransformers;
    private final ExtendedQuerySupport extendedQuerySupport;
    private final Map<String, DbmsDialect> dbmsDialects;
    private final Set<String> aggregateFunctions;
    private final Map<Class<?>, String> treatFunctions;
    private final ExpressionCache expressionCache;
    private final ExpressionFactory expressionFactory;
    private final ExpressionFactory subqueryExpressionFactory;
    private final Map<String, String> properties;

    private final MacroConfiguration macroConfiguration;
    private final String configuredDbms;
    private final DbmsDialect configuredDbmsDialect;
    private final Set<String> configuredRegisteredFunctions;
    private final JpaProviderFactory configuredJpaProviderFactory;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config, EntityManagerFactory entityManagerFactory) {
        this.compatibleMode = Boolean.valueOf(config.getProperty(ConfigurationProperties.COMPATIBLE_MODE));
        this.optimize = PropertyUtils.getAsBooleanProperty(config.getProperties(), ConfigurationProperties.EXPRESSION_OPTIMIZATION, true);

        this.metamodel = new EntityMetamodel(entityManagerFactory.getMetamodel());
        this.queryTransformers = new ArrayList<QueryTransformer>(config.getQueryTransformers());
        this.extendedQuerySupport = config.getExtendedQuerySupport();
        this.dbmsDialects = new HashMap<String, DbmsDialect>(config.getDbmsDialects());
        this.aggregateFunctions = resolveAggregateFunctions(config.getFunctions());
        this.treatFunctions = resolveTreatTypes(config.getTreatTypes());

        ExpressionFactory originalExpressionFactory = new ExpressionFactoryImpl(aggregateFunctions, !compatibleMode, optimize);
        this.expressionCache = createCache(config.getProperty(ConfigurationProperties.EXPRESSION_CACHE_CLASS));
        ExpressionFactory cachingExpressionFactory = new SimpleCachingExpressionFactory(originalExpressionFactory, expressionCache);
        ExpressionFactory cachingSubqueryExpressionFactory = new SimpleCachingExpressionFactory(new SubqueryExpressionFactory(aggregateFunctions, !compatibleMode, optimize, originalExpressionFactory));
        this.macroConfiguration = MacroConfiguration.of(JpqlMacroAdapter.createMacros(config.getMacros(), cachingExpressionFactory));
        JpqlMacroStorage macroStorage = new JpqlMacroStorage(null, macroConfiguration);
        this.expressionFactory = new JpqlMacroAwareExpressionFactory(cachingExpressionFactory, macroStorage);
        this.subqueryExpressionFactory = new JpqlMacroAwareExpressionFactory(cachingSubqueryExpressionFactory, macroStorage);
        this.properties = copyProperties(config.getProperties());
        
        List<EntityManagerFactoryIntegrator> integrators = config.getEntityManagerIntegrators();
        if (integrators.size() < 1) {
            throw new IllegalArgumentException("No EntityManagerFactoryIntegrator was found on the classpath! Please check if an integration for your JPA provider is visible on the classpath!");
        }
        if (integrators.size() > 1) {
            throw new IllegalArgumentException("Multiple EntityManagerFactoryIntegrator were found on the classpath! Please remove the wrong integrations from the classpath!");
        }
        EntityManagerFactoryIntegrator integrator = integrators.get(0);
        EntityManagerFactory emf = integrator.registerFunctions(entityManagerFactory, config.getFunctions());
        Set<String> registeredFunctions = new HashSet<String>(integrator.getRegisteredFunctions(emf));
        String dbms = integrator.getDbms(emf);
        DbmsDialect dialect = dbmsDialects.get(dbms);
        
        // Use the default dialect
        if (dialect == null) {
            dialect = dbmsDialects.get(null);
        }

        this.configuredDbms = dbms;
        this.configuredDbmsDialect = dialect;
        this.configuredRegisteredFunctions = registeredFunctions;
        this.configuredJpaProviderFactory = integrator.getJpaProviderFactory(emf);
    }

    private ExpressionCache createCache(String className) {
        try {
            return (ExpressionCache) Class.forName(className).newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate expression cache: " + className, ex);
        }
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

    private static Map<Class<?>, String> resolveTreatTypes(Map<String, Class<?>> treatTypes) {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>(treatTypes.size());
        for (Map.Entry<String, Class<?>> entry : treatTypes.entrySet()) {
            types.put(entry.getValue(), "TREAT_" + entry.getKey().toUpperCase());
        }
        return Collections.unmodifiableMap(types);
    }

    public JpaProvider createJpaProvider(EntityManager em) {
        return configuredJpaProviderFactory.createJpaProvider(em);
    }

    public boolean isCompatibleMode() {
        return compatibleMode;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public EntityMetamodel getMetamodel() {
        return metamodel;
    }

    public MacroConfiguration getMacroConfiguration() {
        return macroConfiguration;
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

    public Map<Class<?>, String> getTreatFunctions() {
        return treatFunctions;
    }

    public ExpressionCache getExpressionCache() {
        return expressionCache;
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
        if (SubqueryExpressionFactory.class.equals(serviceClass)) {
            return (T) subqueryExpressionFactory;
        } else if (ConfigurationSource.class.equals(serviceClass)) {
            return (T) this;
        } else if (ExpressionFactory.class.isAssignableFrom(serviceClass)) {
            return (T) expressionFactory;
        } else if (DbmsDialect.class.equals(serviceClass)) {
            return (T) configuredDbmsDialect;
        } else if (ExtendedQuerySupport.class.equals(serviceClass)) {
            return (T) extendedQuerySupport;
        } else if (JpaProviderFactory.class.equals(serviceClass)) {
            return (T) configuredJpaProviderFactory;
        } else if (ExpressionCache.class.equals(serviceClass)) {
            return (T) expressionCache;
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
