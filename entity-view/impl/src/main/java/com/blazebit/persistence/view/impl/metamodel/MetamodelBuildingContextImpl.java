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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.MacroFunction;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.impl.JpqlMacroAdapter;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.macro.DefaultViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.BasicUserTypeRegistry;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.BasicUserType;

import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MetamodelBuildingContextImpl implements MetamodelBuildingContext {

    private final Map<Class<?>, Type<?>> basicTypeRegistry = new HashMap<>();
    private final BasicUserTypeRegistry basicUserTypeRegistry;
    private final EntityMetamodel entityMetamodel;
    private final JpaProvider jpaProvider;
    private final Map<String, JpqlFunction> jpqlFunctions;
    private final ExpressionFactory expressionFactory;
    private final ProxyFactory proxyFactory;
    private final Map<Class<?>, ViewMapping> viewMappings;
    private final Set<String> errors;

    private final FlushMode flushModeOverride;
    private final Map<String, FlushMode> flushModeOverrides;
    private final FlushStrategy flushStrategyOverride;
    private final Map<String, FlushStrategy> flushStrategyOverrides;

    public MetamodelBuildingContextImpl(Properties properties, BasicUserTypeRegistry basicUserTypeRegistry, EntityMetamodel entityMetamodel, JpaProvider jpaProvider, Map<String, JpqlFunction> jpqlFunctions, ExpressionFactory expressionFactory, ProxyFactory proxyFactory, Map<Class<?>, ViewMapping> viewMappings, Set<String> errors) {
        this.basicUserTypeRegistry = basicUserTypeRegistry;
        this.entityMetamodel = entityMetamodel;
        this.jpaProvider = jpaProvider;
        this.jpqlFunctions = jpqlFunctions;
        this.expressionFactory = expressionFactory;
        this.proxyFactory = proxyFactory;
        this.viewMappings = viewMappings;
        this.errors = errors;
        this.flushModeOverride = getFlushMode(properties.getProperty(ConfigurationProperties.UPDATER_FLUSH_MODE), "global property '" + ConfigurationProperties.UPDATER_FLUSH_MODE + "'");
        this.flushModeOverrides = getFlushModeOverrides(properties);
        this.flushStrategyOverride = getFlushStrategy(properties.getProperty(ConfigurationProperties.UPDATER_FLUSH_STRATEGY), "global property '" + ConfigurationProperties.UPDATER_FLUSH_STRATEGY + "'");
        this.flushStrategyOverrides = getFlushStrategyOverrides(properties);
    }

    private FlushMode getFlushMode(String property, String location) {
        if (property == null || property.isEmpty()) {
            return null;
        }

        if ("partial".equals(property)) {
            return FlushMode.PARTIAL;
        } else if ("lazy".equals(property)) {
            return FlushMode.LAZY;
        } else if ("full".equals(property)) {
            return FlushMode.FULL;
        }

        throw new IllegalArgumentException("Invalid flush mode defined for " + location + ": " + property);
    }

    private Map<String, FlushMode> getFlushModeOverrides(Properties properties) {
        String prefix = ConfigurationProperties.UPDATER_FLUSH_MODE + ".";
        Map<String, FlushMode> flushModes = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix) && entry.getValue() != null) {
                Object value = entry.getValue();
                FlushMode mode;
                if (value instanceof FlushMode) {
                    mode = (FlushMode) value;
                } else {
                    mode = getFlushMode(entry.getValue().toString(), "property '" + key + "'");
                }
                flushModes.put(key.substring(prefix.length()), mode);
            }
        }
        return flushModes;
    }

    private FlushStrategy getFlushStrategy(String property, String location) {
        if (property == null || property.isEmpty()) {
            return null;
        }

        if ("query".equalsIgnoreCase(property)) {
            return FlushStrategy.QUERY;
        } else if ("entity".equalsIgnoreCase(property)) {
            return FlushStrategy.ENTITY;
        }

        throw new IllegalArgumentException("Invalid flush strategy defined for " + location + ": " + property);
    }

    private Map<String, FlushStrategy> getFlushStrategyOverrides(Properties properties) {
        String prefix = ConfigurationProperties.UPDATER_FLUSH_STRATEGY + ".";
        Map<String, FlushStrategy> flushStrategies = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix) && entry.getValue() != null) {
                Object value = entry.getValue();
                FlushStrategy strategy;
                if (value instanceof FlushStrategy) {
                    strategy = (FlushStrategy) value;
                } else {
                    strategy = getFlushStrategy(entry.getValue().toString(), "property '" + key + "'");
                }
                flushStrategies.put(key.substring(prefix.length()), strategy);
            }
        }
        return flushStrategies;
    }

    @Override
    public Map<Class<?>, ViewMapping> getViewMappings() {
        return viewMappings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Type<X> getBasicType(Class<X> basicClass) {
        if (basicClass == null) {
            return null;
        }

        Type<X> t = (Type<X>) basicTypeRegistry.get(basicClass);
        if (t == null) {
            BasicUserType<X> userType = basicUserTypeRegistry.getBasicUserType(basicClass);
            ManagedType<X> managedType = entityMetamodel.getManagedType(basicClass);
            t = new BasicTypeImpl<>(basicClass, managedType, userType);
            basicTypeRegistry.put(basicClass, t);
        }
        return t;
    }

    @Override
    public Map<String, JpqlFunction> getJpqlFunctions() {
        return jpqlFunctions;
    }

    @Override
    public EntityMetamodel getEntityMetamodel() {
        return entityMetamodel;
    }

    @Override
    public JpaProvider getJpaProvider() {
        return jpaProvider;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    @Override
    public ExpressionFactory createMacroAwareExpressionFactory() {
        return createMacroAwareExpressionFactory("syntax_checking_placeholder");
    }

    @Override
    public ExpressionFactory createMacroAwareExpressionFactory(String viewRoot) {
        MacroConfiguration originalMacroConfiguration = expressionFactory.getDefaultMacroConfiguration();
        ExpressionFactory cachingExpressionFactory = expressionFactory.unwrap(AbstractCachingExpressionFactory.class);
        MacroFunction macro = new JpqlMacroAdapter(new DefaultViewRootJpqlMacro(viewRoot), cachingExpressionFactory);
        MacroConfiguration macroConfiguration = originalMacroConfiguration.with(Collections.singletonMap("view_root", macro));
        return new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
    }

    @Override
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    @Override
    public FlushMode getFlushMode(Class<?> clazz, FlushMode defaultValue) {
        if (flushModeOverride != null) {
            return flushModeOverride;
        }
        FlushMode mode = flushModeOverrides.get(clazz.getName());
        if (mode != null) {
            return mode;
        } else {
            return defaultValue;
        }
    }

    @Override
    public FlushStrategy getFlushStrategy(Class<?> clazz, FlushStrategy defaultValue) {
        if (flushStrategyOverride != null) {
            return flushStrategyOverride;
        }
        FlushStrategy mode = flushStrategyOverrides.get(clazz.getName());
        if (mode != null) {
            return mode;
        } else {
            return defaultValue;
        }
    }

    @Override
    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public boolean isEntityView(Class<?> clazz) {
        return viewMappings.containsKey(clazz);
    }

    @Override
    public Set<Class<?>> findSubtypes(Class<?> entityViewClass) {
        Set<Class<?>> subtypes = new HashSet<>();
        for (Class<?> clazz : viewMappings.keySet()) {
            if (entityViewClass.isAssignableFrom(clazz) && entityViewClass != clazz) {
                subtypes.add(clazz);
            }
        }

        return subtypes;
    }
}
