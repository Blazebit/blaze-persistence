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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
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
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.UpdateableProxy;
import com.blazebit.persistence.view.impl.update.EntityViewUpdater;
import com.blazebit.persistence.view.impl.update.FullEntityViewUpdater;
import com.blazebit.persistence.view.impl.update.PartialEntityViewUpdater;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewManagerImpl implements EntityViewManager {

    private final ViewMetamodel metamodel;
    private final ProxyFactory proxyFactory;
    private final Map<String, Object> properties;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final ConcurrentMap<ViewType<?>, PartialEntityViewUpdater> partialEntityViewUpdaterCache;
    private final ConcurrentMap<ViewType<?>, FullEntityViewUpdater> fullEntityViewUpdaterCache;
    private final Map<String, Class<? extends AttributeFilterProvider>> filterMappings;
    
    private final boolean unsafeDisabled;

    public EntityViewManagerImpl(EntityViewConfigurationImpl config) {
        this.metamodel = new ViewMetamodelImpl(config.getEntityViews());
        this.proxyFactory = new ProxyFactory();
        this.properties = copyProperties(config.getProperties());
        this.objectBuilderCache = new ConcurrentHashMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>>();
        this.partialEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, PartialEntityViewUpdater>();
        this.fullEntityViewUpdaterCache = new ConcurrentHashMap<ViewType<?>, FullEntityViewUpdater>();
        this.filterMappings = new HashMap<String, Class<? extends AttributeFilterProvider>>();
        registerFilterMappings();
        
        this.unsafeDisabled = !Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_UNSAFE_ALLOWED)));
        
        if (Boolean.valueOf(String.valueOf(properties.get(ConfigurationProperties.PROXY_EAGER_LOADING)))) {
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
    public ViewMetamodel getMetamodel() {
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
        if (!(view instanceof UpdateableProxy)) {
            throw new IllegalArgumentException("Only updateable entity views can be updated!");
        }
        
        UpdateableProxy updateableProxy = (UpdateableProxy) view;
        Class<?> entityViewClass = updateableProxy.$$_getEntityViewClass();
        ViewType<?> viewType = metamodel.view(entityViewClass);
        partial = viewType.isPartiallyUpdateable() && partial;
        EntityViewUpdater updater = getUpdater(viewType, partial);
        updater.executeUpdate(em, updateableProxy);
    }

    @Override
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder) {
        return EntityViewSettingHelper.apply(setting, this, criteriaBuilder);
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
	public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, PaginatedCriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        if (viewType == null) {
        	throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (FullQueryBuilder<?, ?>) criteriaBuilder, optionalParameters);
        return (PaginatedCriteriaBuilder<T>) criteriaBuilder;
    }

    @SuppressWarnings("unchecked")
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        if (viewType == null) {
        	throw new IllegalArgumentException("There is no entity view for the class '" + clazz.getName() + "' registered!");
        }
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (FullQueryBuilder<?, ?>) criteriaBuilder, optionalParameters);
        return (CriteriaBuilder<T>) criteriaBuilder;
    }

    private <T> void applyObjectBuilder(ViewType<T> viewType, MappingConstructor<T> mappingConstructor, FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters) {
        if (!viewType.getEntityClass().isAssignableFrom(criteriaBuilder.getResultType())) {
            throw new IllegalArgumentException("The given view type with the entity type '" + viewType.getEntityClass().getName()
                + "' can not be applied to the query builder with result type '" + criteriaBuilder.getResultType().getName() + "'");
        }

        criteriaBuilder.selectNew(getTemplate(criteriaBuilder, viewType, mappingConstructor).createObjectBuilder(criteriaBuilder, new HashMap<String, Object>(optionalParameters)));
    }

    @SuppressWarnings("unchecked")
    private <T> ViewTypeObjectBuilderTemplate<T> getTemplate(FullQueryBuilder<?, ?> cb, ViewType<T> viewType, MappingConstructor<T> mappingConstructor) {
    	ExpressionFactory ef = cb.getCriteriaBuilderFactory().getService(ExpressionFactory.class);
    	ViewTypeObjectBuilderTemplate.Key<T> key = new ViewTypeObjectBuilderTemplate.Key<T>(ef, viewType, mappingConstructor);
        ViewTypeObjectBuilderTemplate<?> value = objectBuilderCache.get(key);

        if (value == null) {
        	Metamodel jpaMetamodel = cb.getMetamodel();
            value = key.createValue(jpaMetamodel, this, proxyFactory);
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
