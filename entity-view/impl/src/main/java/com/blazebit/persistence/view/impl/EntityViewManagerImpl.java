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
package com.blazebit.persistence.view.impl;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.*;
import com.blazebit.persistence.impl.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.MacroFunction;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.ContainsIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EndsWithFilter;
import com.blazebit.persistence.view.filter.EndsWithIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EqualFilter;
import com.blazebit.persistence.view.filter.GreaterOrEqualFilter;
import com.blazebit.persistence.view.filter.GreaterThanFilter;
import com.blazebit.persistence.view.filter.LessOrEqualFilter;
import com.blazebit.persistence.view.filter.LessThanFilter;
import com.blazebit.persistence.view.filter.NullFilter;
import com.blazebit.persistence.view.filter.StartsWithFilter;
import com.blazebit.persistence.view.filter.StartsWithIgnoreCaseFilter;
import com.blazebit.persistence.view.impl.filter.ContainsFilterImpl;
import com.blazebit.persistence.view.impl.filter.ContainsIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.NullFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.macro.ViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.UpdatableProxy;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.FullEntityViewUpdater;
import com.blazebit.persistence.view.impl.update.PartialEntityViewUpdater;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewManagerImpl implements EntityViewManager {

    private final ViewMetamodelImpl metamodel;
    private final ProxyFactory proxyFactory;
    private final Map<String, Object> properties;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final ConcurrentMap<ViewType<?>, PartialEntityViewUpdater> partialEntityViewUpdaterCache;
    private final ConcurrentMap<ViewType<?>, FullEntityViewUpdater> fullEntityViewUpdaterCache;
    private final Map<String, Class<? extends AttributeFilterProvider>> filterMappings;
    
    private final boolean unsafeDisabled;

    public EntityViewManagerImpl(EntityViewConfigurationImpl config, CriteriaBuilderFactory cbf, EntityManagerFactory entityManagerFactory) {
        this.metamodel = new ViewMetamodelImpl(config.getEntityViews(), !Boolean.valueOf(String.valueOf(config.getProperty(ConfigurationProperties.EXPRESSION_VALIDATION_DISABLED))), cbf.getService(ExpressionFactory.class), entityManagerFactory.getMetamodel());
        this.proxyFactory = new ProxyFactory();
        this.properties = copyProperties(config.getProperties());
        this.objectBuilderCache = new ConcurrentHashMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>>();
        this.partialEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, PartialEntityViewUpdater>();
        this.fullEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, FullEntityViewUpdater>();
        this.filterMappings = new HashMap<String, Class<? extends AttributeFilterProvider>>();
        registerFilterMappings();
        
        this.unsafeDisabled = !Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        
        if (Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.TEMPLATE_EAGER_LOADING)))) {
            MacroConfiguration originalMacroConfiguration = cbf.getService(MacroConfiguration.class);
            ExpressionFactory cachingExpressionFactory = cbf.getService(ExpressionFactory.class).unwrap(AbstractCachingExpressionFactory.class);
        	for (ViewType<?> view : metamodel.getViews()) {
                MacroFunction macro = new JpqlMacroAdapter(new ViewRootJpqlMacro(null), cachingExpressionFactory);
        	    MacroConfiguration macroConfiguration = originalMacroConfiguration.with(Collections.singletonMap("view_root", macro));
        	    ExpressionFactory expressionFactory = new MacroConfigurationExpressionFactory(cachingExpressionFactory, macroConfiguration);
                getTemplate(expressionFactory, view, null, null);

                for (MappingConstructor<?> constructor : view.getConstructors()) {
                    getTemplate(expressionFactory, view, (MappingConstructor) constructor, null);
                }
        	}
        } else if (Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_EAGER_LOADING)))) {
            for (ViewType<?> view : metamodel.getViews()) {
                if (view.getConstructors().isEmpty() || unsafeDisabled) {
                    proxyFactory.getProxy(view);
                } else {
                    proxyFactory.getUnsafeProxy(view);
                }
            }
        }
    }

    @Override
    public ViewMetamodelImpl getMetamodel() {
        return metamodel;
    }

    @Override
    public void update(EntityManager em, Object view) {
    	update(em, view, true);
    }

    @Override
    public void updateFull(EntityManager em, Object view) {
    	update(em, view, false);
    }
    
    private void update(EntityManager em, Object view, boolean partial) {
        if (!(view instanceof UpdatableProxy)) {
            throw new IllegalArgumentException("Only updatable entity views can be updated!");
        }
        
        UpdatableProxy updatableProxy = (UpdatableProxy) view;
        Class<?> entityViewClass = updatableProxy.$$_getEntityViewClass();
        ViewType<?> viewType = metamodel.view(entityViewClass);
        partial = viewType.isPartiallyUpdatable() && partial;
        EntityViewUpdater updater = getUpdater(viewType, partial);
        updater.executeUpdate(em, updatableProxy);
    }

    @Override
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder) {
        return EntityViewSettingHelper.apply(setting, this, criteriaBuilder, null);
    }

    @Override
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot) {
        return EntityViewSettingHelper.apply(setting, this, criteriaBuilder, entityViewRoot);
    }

	public boolean isUnsafeDisabled() {
		return unsafeDisabled;
	}

    /**
     * Creates a new filter instance of the given filter class.
     *
     * @param <T>         The filter type
     * @param filterClass The filter class
     * @return An instance of the given filter class
     */
    public <T extends ViewFilterProvider> T createViewFilter(Class<T> filterClass) {
        try {
            return filterClass.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the view filter class: " + filterClass.getName(), ex);
        }
    }

    /**
     * Creates a new filter instance of the given filter class. If the filter class is a registered placeholder, the real
     * implementation will be resolved and instantiated.
     *
     * This method tries to instantiate an object by invoking one of the allowed constructors as defined in {@link AttributeFilterProvider}
     *
     * @param <T>          The filter type
     * @param filterClass  The filter class or a filter placeholder
     * @param expectedType The expected type of the argument into which it should be converted to
     * @param argument     The filter argument which is passed to the filter constructor
     * @return An instance of the given filter class
     */
    public <T extends AttributeFilterProvider> T createAttributeFilter(Class<T> filterClass, Class<?> expectedType, Object argument) {
        @SuppressWarnings("unchecked")
		Class<T> filterClassImpl = (Class<T>) filterMappings.get(filterClass.getName());

        if (filterClassImpl == null) {
            return createFilterInstance(filterClass, expectedType, argument);
        } else {
            return createFilterInstance(filterClassImpl, expectedType, argument);
        }
    }

    private <T extends AttributeFilterProvider> T createFilterInstance(Class<T> filterClass, Class<?> expectedType, Object argument) {
        try {
            @SuppressWarnings("unchecked")
			Constructor<T>[] constructors = (Constructor<T>[]) filterClass.getDeclaredConstructors();
            Constructor<T> filterConstructor = findConstructor(constructors, Class.class, Object.class);

            if (filterConstructor != null) {
                return filterConstructor.newInstance(expectedType, argument);
            } else {
                filterConstructor = findConstructor(constructors, Class.class);

                if (filterConstructor != null) {
                    return filterConstructor.newInstance(expectedType);
                } else {
                    filterConstructor = findConstructor(constructors, Object.class);

                    if (filterConstructor != null) {
                        return filterConstructor.newInstance(argument);
                    } else {
                        filterConstructor = findConstructor(constructors);

                        if (filterConstructor == null) {
                            throw new IllegalArgumentException("No suitable constructor found for filter class '" + filterClass.getName() + "'");
                        }

                        return filterConstructor.newInstance();
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create an instance of the filter class '" + filterClass.getName() + "'", ex);
        }
    }

    private <T> Constructor<T> findConstructor(Constructor<T>[] constructors, Class<?>... classes) {
        for (int i = 0; i < constructors.length; i++) {
            if (Arrays.equals(constructors[i].getParameterTypes(), classes)) {
                return (Constructor<T>) constructors[i];
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
	public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, String entityViewRoot, PaginatedCriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration configuration) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        if (viewType == null) {
        	throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, viewType.getName(), entityViewRoot, (FullQueryBuilder<?, ?>) criteriaBuilder, optionalParameters, configuration, 0, true);
        return (PaginatedCriteriaBuilder<T>) criteriaBuilder;
    }

    @SuppressWarnings("unchecked")
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, String entityViewRoot, CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration configuration) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        if (viewType == null) {
        	throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, viewType.getName(), entityViewRoot, (FullQueryBuilder<?, ?>) criteriaBuilder, optionalParameters, configuration, 0, true);
        return (CriteriaBuilder<T>) criteriaBuilder;
    }

    public <T> void applyObjectBuilder(ViewType<T> viewType, MappingConstructor<T> mappingConstructor, String viewName, String entityViewRoot, FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration configuration, int offset, boolean registerMacro) {
        Class<?> entityClazz;
        Set<Root> roots = criteriaBuilder.getRoots();
        Map.Entry<Root, String> rootEntry = findRoot(roots, entityViewRoot);
        Root root = rootEntry.getKey();
        entityViewRoot = rootEntry.getValue();
        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        if (entityViewRoot != null) {
            if (root.getAlias().equals(entityViewRoot)) {
                entityClazz = root.getType();
            } else {
                PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(metamodel.getEntityMetamodel(), root.getType());
                ef.createPathExpression(entityViewRoot.substring(root.getAlias().length() + 1)).accept(visitor);
                Collection<Class<?>> possibleTypes = visitor.getPossibleTargets().values();
                if (possibleTypes.size() > 1) {
                    throw new IllegalArgumentException("The expression '" + entityViewRoot + "' is ambiguous in the context of the type '" + root.getType() + "'!");
                }
                // It must have one, otherwise a parse error would have been thrown already
                entityClazz = possibleTypes.iterator().next();

                if (entityClazz == null) {
                    throw new IllegalArgumentException("Could not resolve the expression '" + entityViewRoot + "' in the context of the type '" + root.getType() + "'!");
                }
            }
        } else {
            entityClazz = root.getType();
            entityViewRoot = root.getAlias();
        }
        if (!viewType.getEntityClass().isAssignableFrom(entityClazz)) {
            throw new IllegalArgumentException("The given view type with the entity type '" + viewType.getEntityClass().getName()
                + "' can not be applied to the query builder with result type '" + criteriaBuilder.getResultType().getName() + "'");
        }

        if (registerMacro) {
            criteriaBuilder.registerMacro("view_root", new ViewRootJpqlMacro(entityViewRoot));
        }
        criteriaBuilder.selectNew(getTemplate(ef, viewType, mappingConstructor, viewName, entityViewRoot, offset).createObjectBuilder(criteriaBuilder, new HashMap<String, Object>(optionalParameters), configuration));
    }

    private static Map.Entry<Root, String> findRoot(Set<Root> roots, String entityViewRoot) {
        if (entityViewRoot == null || entityViewRoot.isEmpty()) {
            if (roots.size() > 1) {
                throw new IllegalArgumentException("Can not apply entity view to given criteria builder because it has multiple query roots! Please specify the entity view root!");
            }

            return new AbstractMap.SimpleEntry<Root, String>(roots.iterator().next(), null);
        }

        if (roots.size() == 1) {
            Root r = roots.iterator().next();
            String alias = r.getAlias();
            if (entityViewRoot.startsWith(alias) && (entityViewRoot.length() == alias.length() || entityViewRoot.charAt(alias.length()) == '.')) {
                return new AbstractMap.SimpleEntry<Root, String>(r, entityViewRoot);
            } else {
                return new AbstractMap.SimpleEntry<Root, String>(r, alias + '.' + entityViewRoot);
            }
        }

        for (Root r : roots) {
            String alias = r.getAlias();
            if (entityViewRoot.startsWith(alias) && (entityViewRoot.length() == alias.length() || entityViewRoot.charAt(alias.length()) == '.')) {
                return new AbstractMap.SimpleEntry<Root, String>(r, entityViewRoot);
            }
        }

        throw new IllegalArgumentException("Entity view root '" + entityViewRoot + "' must be an absolute path when multiple criteria builder roots are possible!");
    }

    @SuppressWarnings("unchecked")
    public <T> ViewTypeObjectBuilderTemplate<T> getTemplate(ExpressionFactory ef, ViewType<T> viewType, MappingConstructor<T> mappingConstructor, String entityViewRoot) {
        return getTemplate(ef, viewType, mappingConstructor, viewType.getName(), entityViewRoot, 0);
    }

    public <T> ViewTypeObjectBuilderTemplate<T> getTemplate(ExpressionFactory ef, ManagedViewType<T> viewType, MappingConstructor<T> mappingConstructor, String name, String entityViewRoot, int offset) {
    	ViewTypeObjectBuilderTemplate.Key<T> key = new ViewTypeObjectBuilderTemplate.Key<T>(ef, viewType, mappingConstructor, name, entityViewRoot, offset);
        ViewTypeObjectBuilderTemplate<?> value = objectBuilderCache.get(key);

        if (value == null) {
            value = key.createValue(this, proxyFactory);
            ViewTypeObjectBuilderTemplate<?> oldValue = objectBuilderCache.putIfAbsent(key, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return (ViewTypeObjectBuilderTemplate<T>) value;
    }
    
    private EntityViewUpdater getUpdater(ViewType<?> viewType, boolean partial) {
    	if (partial) {
    		return getPartialUpdater(viewType);
    	} else {
    		return getFullUpdater(viewType);
    	}
    }
    
    private FullEntityViewUpdater getFullUpdater(ViewType<?> viewType) {
    	FullEntityViewUpdater value = fullEntityViewUpdaterCache.get(viewType);

        if (value == null) {
            value = new FullEntityViewUpdater(viewType);
            FullEntityViewUpdater oldValue = fullEntityViewUpdaterCache.putIfAbsent(viewType, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }
    
    private PartialEntityViewUpdater getPartialUpdater(ViewType<?> viewType) {
    	PartialEntityViewUpdater value = partialEntityViewUpdaterCache.get(viewType);

        if (value == null) {
            value = new PartialEntityViewUpdater(viewType);
            PartialEntityViewUpdater oldValue = partialEntityViewUpdaterCache.putIfAbsent(viewType, value);

            if (oldValue != null) {
                value = oldValue;
            }
        }

        return value;
    }

    private void registerFilterMappings() {
        filterMappings.put(ContainsFilter.class.getName(), ContainsFilterImpl.class);
        filterMappings.put(ContainsIgnoreCaseFilter.class.getName(), ContainsIgnoreCaseFilterImpl.class);
        filterMappings.put(StartsWithFilter.class.getName(), StartsWithFilterImpl.class);
        filterMappings.put(StartsWithIgnoreCaseFilter.class.getName(), StartsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(EndsWithFilter.class.getName(), EndsWithFilterImpl.class);
        filterMappings.put(EndsWithIgnoreCaseFilter.class.getName(), EndsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(EqualFilter.class.getName(), EqualFilterImpl.class);
        filterMappings.put(NullFilter.class.getName(), NullFilterImpl.class);
        filterMappings.put(GreaterThanFilter.class.getName(), GreaterThanFilterImpl.class);
        filterMappings.put(GreaterOrEqualFilter.class.getName(), GreaterOrEqualFilterImpl.class);
        filterMappings.put(LessThanFilter.class.getName(), LessThanFilterImpl.class);
        filterMappings.put(LessOrEqualFilter.class.getName(), LessOrEqualFilterImpl.class);
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
