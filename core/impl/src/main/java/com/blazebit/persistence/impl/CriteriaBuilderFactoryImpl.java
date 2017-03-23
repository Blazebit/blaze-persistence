/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.impl.expression.ExpressionCache;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunctionGroup;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final EntityMetamodelImpl metamodel;
    private final AssociationParameterTransformerFactory transientEntityParameterTransformerFactory;
    private final ExtendedQuerySupport extendedQuerySupport;
    private final Set<String> aggregateFunctions;
    private final Map<Class<?>, String> treatFunctions;
    private final ExpressionCache expressionCache;
    private final ExpressionFactory expressionFactory;
    private final ExpressionFactory subqueryExpressionFactory;
    private final QueryConfiguration queryConfiguration;

    private final MacroConfiguration macroConfiguration;
    private final String configuredDbms;
    private final DbmsDialect configuredDbmsDialect;
    private final Set<String> configuredRegisteredFunctions;
    private final JpaProviderFactory configuredJpaProviderFactory;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config, EntityManagerFactory entityManagerFactory) {
        this.queryConfiguration = new ImmutableQueryConfiguration((Map<String, String>) (Map<?, ?>) config.getProperties());
        final boolean compatibleMode = queryConfiguration.isCompatibleModeEnabled();
        final boolean optimize = queryConfiguration.isExpressionOptimizationEnabled();

        this.metamodel = new EntityMetamodelImpl(entityManagerFactory, config.getExtendedQuerySupport());
        this.transientEntityParameterTransformerFactory = new TransientEntityAssociationParameterTransformerFactory(metamodel, new AssociationToIdParameterTransformer(entityManagerFactory.getPersistenceUnitUtil()));
        this.extendedQuerySupport = config.getExtendedQuerySupport();
        this.aggregateFunctions = resolveAggregateFunctions(config.getFunctions());
        this.treatFunctions = resolveTreatTypes(config.getTreatTypes());

        ExpressionFactory originalExpressionFactory = new ExpressionFactoryImpl(aggregateFunctions, metamodel.getEntityTypes(), metamodel.getEnumTypes(), !compatibleMode, optimize);
        this.expressionCache = createCache(queryConfiguration.getExpressionCacheClass());
        ExpressionFactory cachingExpressionFactory = new SimpleCachingExpressionFactory(originalExpressionFactory, expressionCache);
        ExpressionFactory cachingSubqueryExpressionFactory = new SimpleCachingExpressionFactory(new SubqueryExpressionFactory(aggregateFunctions, metamodel.getEntityTypes(), metamodel.getEnumTypes(), !compatibleMode, optimize, originalExpressionFactory));
        this.macroConfiguration = MacroConfiguration.of(JpqlMacroAdapter.createMacros(config.getMacros(), cachingExpressionFactory));
        JpqlMacroStorage macroStorage = new JpqlMacroStorage(null, macroConfiguration);
        this.expressionFactory = new JpqlMacroAwareExpressionFactory(cachingExpressionFactory, macroStorage);
        this.subqueryExpressionFactory = new JpqlMacroAwareExpressionFactory(cachingSubqueryExpressionFactory, macroStorage);

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
        Map<String, DbmsDialect> dbmsDialects = config.getDbmsDialects();
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

    public QueryConfiguration getQueryConfiguration() {
        return queryConfiguration;
    }

    public EntityMetamodelImpl getMetamodel() {
        return metamodel;
    }

    public AssociationParameterTransformerFactory getTransientEntityParameterTransformerFactory() {
        return transientEntityParameterTransformerFactory;
    }

    public MacroConfiguration getMacroConfiguration() {
        return macroConfiguration;
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
        return queryConfiguration.getProperties();
    }

    @Override
    public String getProperty(String propertyName) {
        return queryConfiguration.getProperty(propertyName);
    }
    
    public MainQuery createMainQuery(EntityManager entityManager) {
        return MainQuery.create(this, entityManager, configuredDbms, configuredDbmsDialect, configuredRegisteredFunctions);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>> startSet(EntityManager entityManager, Class<T> resultClass) {
        MainQuery mainQuery = createMainQuery(entityManager);
        FinalSetOperationCriteriaBuilderImpl<T> parentFinalSetOperationBuilder = new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, true, resultClass, null, false, null);
        OngoingFinalSetOperationCriteriaBuilderImpl<T> subFinalSetOperationBuilder = new OngoingFinalSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultClass, null, true, parentFinalSetOperationBuilder.getSubListener());
        
        LeafOngoingSetOperationCriteriaBuilderImpl<T> leafCb = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultClass, parentFinalSetOperationBuilder.getSubListener(), parentFinalSetOperationBuilder);
        StartOngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>> cb = new StartOngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>>(mainQuery, false, resultClass, subFinalSetOperationBuilder.getSubListener(), subFinalSetOperationBuilder, leafCb);
        
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
        } else if (EntityMetamodel.class.equals(serviceClass)) {
            return (T) metamodel;
        }

        return null;
    }

}
