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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCache;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.parser.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.spi.PackageOpener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final PackageOpener packageOpener;
    private final EntityManagerFactory entityManagerFactory;
    private final EntityMetamodelImpl metamodel;
    private final AssociationParameterTransformerFactory transientEntityParameterTransformerFactory;
    private final ExtendedQuerySupport extendedQuerySupport;
    private final Set<String> aggregateFunctions;
    private final Map<Class<?>, String> namedTypes;
    private final ExpressionCache expressionCache;
    private final ExpressionFactory expressionFactory;
    private final ExpressionFactory subqueryExpressionFactory;
    private final QueryConfiguration queryConfiguration;

    private final MacroConfiguration macroConfiguration;
    private final String configuredDbms;
    private final DbmsDialect configuredDbmsDialect;
    private final Map<String, JpqlFunction> configuredRegisteredFunctions;
    private final JpaProviderFactory configuredJpaProviderFactory;
    private final JpaProvider jpaProvider;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config, EntityManagerFactory entityManagerFactory) {
        List<EntityManagerFactoryIntegrator> integrators = config.getEntityManagerIntegrators();
        if (integrators.size() < 1) {
            throw new IllegalArgumentException("No EntityManagerFactoryIntegrator was found on the classpath! Please check if an integration for your JPA provider is visible on the classpath!");
        }
        if (integrators.size() > 1) {
            throw new IllegalArgumentException("Multiple EntityManagerFactoryIntegrator were found on the classpath! Please remove the wrong integrations from the classpath!");
        }
        EntityManagerFactoryIntegrator integrator = integrators.get(0);
        EntityManagerFactory emf = integrator.registerFunctions(entityManagerFactory, config.getFunctions());
        Map<String, JpqlFunction> registeredFunctions = new HashMap<>(integrator.getRegisteredFunctions(emf));
        String dbms = integrator.getDbms(emf);
        Map<String, DbmsDialect> dbmsDialects = config.getDbmsDialects();
        DbmsDialect dialect = dbmsDialects.get(dbms);

        // Use the default dialect
        if (dialect == null) {
            dialect = dbmsDialects.get(null);
        }

        this.packageOpener = config.getPackageOpener();
        this.configuredDbms = dbms;
        this.configuredDbmsDialect = dialect;
        this.configuredRegisteredFunctions = registeredFunctions;
        this.configuredJpaProviderFactory = integrator.getJpaProviderFactory(emf);

        this.queryConfiguration = new ImmutableQueryConfiguration((Map<String, String>) (Map<?, ?>) config.getProperties());
        final boolean compatibleMode = queryConfiguration.isCompatibleModeEnabled();
        final boolean optimize = queryConfiguration.isExpressionOptimizationEnabled();

        this.entityManagerFactory = entityManagerFactory;
        this.metamodel = new EntityMetamodelImpl(entityManagerFactory, configuredJpaProviderFactory);
        this.jpaProvider = new CachingJpaProvider(metamodel);

        this.transientEntityParameterTransformerFactory = new TransientEntityAssociationParameterTransformerFactory(metamodel, new AssociationToIdParameterTransformer(jpaProvider));
        this.extendedQuerySupport = config.getExtendedQuerySupport();
        this.aggregateFunctions = resolveAggregateFunctions(config.getFunctions());
        this.namedTypes = resolveNamedTypes(config.getNamedTypes());

        ExpressionFactory originalExpressionFactory = new ExpressionFactoryImpl(aggregateFunctions, metamodel.getEntityTypes(), metamodel.getEnumTypes(), !compatibleMode, optimize);
        this.expressionCache = createCache(queryConfiguration.getExpressionCacheClass());
        ExpressionFactory cachingExpressionFactory = new SimpleCachingExpressionFactory(originalExpressionFactory, expressionCache);
        ExpressionFactory cachingSubqueryExpressionFactory = new SimpleCachingExpressionFactory(new SubqueryExpressionFactory(aggregateFunctions, metamodel.getEntityTypes(), metamodel.getEnumTypes(), !compatibleMode, optimize, originalExpressionFactory));
        this.macroConfiguration = MacroConfiguration.of(JpqlMacroAdapter.createMacros(config.getMacros(), cachingExpressionFactory));
        JpqlMacroStorage macroStorage = new JpqlMacroStorage(null, macroConfiguration);
        this.expressionFactory = new JpqlMacroAwareExpressionFactory(cachingExpressionFactory, macroStorage);
        this.subqueryExpressionFactory = new JpqlMacroAwareExpressionFactory(cachingSubqueryExpressionFactory, macroStorage);
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
        // add standard JPQL aggregate functions
        aggregateFunctions.add("sum");
        aggregateFunctions.add("min");
        aggregateFunctions.add("max");
        aggregateFunctions.add("avg");
        aggregateFunctions.add("count");
        return aggregateFunctions;
    }

    private static Map<Class<?>, String> resolveNamedTypes(Map<String, Class<?>> namedTypes) {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>(namedTypes.size());
        for (Map.Entry<String, Class<?>> entry : namedTypes.entrySet()) {
            types.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(types);
    }

    public JpaProvider getJpaProvider() {
        return jpaProvider;
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

    public Map<Class<?>, String> getNamedTypes() {
        return namedTypes;
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
    public Map<String, JpqlFunction> getRegisteredFunctions() {
        return Collections.unmodifiableMap(configuredRegisteredFunctions);
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
        FinalSetOperationCriteriaBuilderImpl<T> parentFinalSetOperationBuilder = new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, null, true, resultClass, null, false, null);
        OngoingFinalSetOperationCriteriaBuilderImpl<T> subFinalSetOperationBuilder = new OngoingFinalSetOperationCriteriaBuilderImpl<T>(mainQuery, null, false, resultClass, null, true, parentFinalSetOperationBuilder.getSubListener());
        
        LeafOngoingSetOperationCriteriaBuilderImpl<T> leafCb = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, null, false, resultClass, parentFinalSetOperationBuilder.getSubListener(), parentFinalSetOperationBuilder);
        StartOngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>> cb = new StartOngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>>(mainQuery, null, false, resultClass, subFinalSetOperationBuilder.getSubListener(), subFinalSetOperationBuilder, leafCb);
        
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
    public <T> DeleteCriteriaBuilder<T> deleteCollection(EntityManager entityManager, Class<T> deleteOwnerClass, String collectionName) {
        return deleteCollection(entityManager, deleteOwnerClass, null, collectionName);
    }

    @Override
    public <T> DeleteCriteriaBuilder<T> deleteCollection(EntityManager entityManager, Class<T> deleteOwnerClass, String alias, String collectionName) {
        MainQuery mainQuery = createMainQuery(entityManager);
        DeleteCollectionCriteriaBuilderImpl<T> cb = new DeleteCollectionCriteriaBuilderImpl<T>(mainQuery, deleteOwnerClass, alias, collectionName);
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
    public <T> UpdateCriteriaBuilder<T> updateCollection(EntityManager entityManager, Class<T> updateOwnerClass, String collectionName) {
        return updateCollection(entityManager, updateOwnerClass, null, collectionName);
    }

    @Override
    public <T> UpdateCriteriaBuilder<T> updateCollection(EntityManager entityManager, Class<T> updateOwnerClass, String alias, String collectionName) {
        MainQuery mainQuery = createMainQuery(entityManager);
        UpdateCollectionCriteriaBuilderImpl<T> cb = new UpdateCollectionCriteriaBuilderImpl<T>(mainQuery, updateOwnerClass, alias, collectionName);
        return cb;
    }

    @Override
    public <T> InsertCriteriaBuilder<T> insert(EntityManager entityManager, Class<T> insertClass) {
        MainQuery mainQuery = createMainQuery(entityManager);
        InsertCriteriaBuilderImpl<T> cb = new InsertCriteriaBuilderImpl<T>(mainQuery, insertClass);
        return cb;
    }

    @Override
    public <T> InsertCriteriaBuilder<T> insertCollection(EntityManager entityManager, Class<T> insertOwnerClass, String collectionName) {
        MainQuery mainQuery = createMainQuery(entityManager);
        InsertCollectionCriteriaBuilderImpl<T> cb = new InsertCollectionCriteriaBuilderImpl<T>(mainQuery, insertOwnerClass, collectionName);
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
        } else if (JpaProvider.class.equals(serviceClass)) {
            return (T) jpaProvider;
        } else if (ExpressionCache.class.equals(serviceClass)) {
            return (T) expressionCache;
        } else if (Metamodel.class.isAssignableFrom(serviceClass)) {
            return (T) metamodel;
        } else if (EntityManagerFactory.class.equals(serviceClass)) {
            return (T) entityManagerFactory;
        } else if (PackageOpener.class.equals(serviceClass)) {
            if (CallerChecker.isCallerTrusted()) {
                return (T) packageOpener;
            }
        }

        return null;
    }

}
