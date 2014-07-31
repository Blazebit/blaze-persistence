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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.spi.ObjectBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.ContainsIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EndsWithFilter;
import com.blazebit.persistence.view.filter.EndsWithIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.ExactFilter;
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
import com.blazebit.persistence.view.impl.filter.ExactFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.GreaterThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessOrEqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.LessThanFilterImpl;
import com.blazebit.persistence.view.impl.filter.NullFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author cpbec
 */
public class EntityViewManagerImpl implements EntityViewManager {
    
    private final ViewMetamodel metamodel;
    private final ProxyFactory proxyFactory;
    private final ConcurrentMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>> objectBuilderCache;
    private final Map<String, Class<? extends Filter>> filterMappings;
    
    public EntityViewManagerImpl(EntityViewConfigurationImpl config) {
        this.metamodel = new ViewMetamodelImpl(config.getEntityViews());
        this.proxyFactory = new ProxyFactory();
        this.objectBuilderCache = new ConcurrentHashMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>>();
        this.filterMappings = new HashMap<String, Class<? extends Filter>>();
        registerFilterMappings();
    }

    @Override
    public ViewMetamodel getMetamodel() {
        return metamodel;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> filterClass, Class<?> expectedType, Object argument) {
        Class<T> filterClassImpl = (Class<T>) filterMappings.get(filterClass.getName());
        
        if (filterClassImpl == null) {
            return createFilterInstance(filterClass, expectedType, argument);
        } else {
            return createFilterInstance(filterClassImpl, expectedType, argument);
        }
    }
    
    private <T extends Filter> T createFilterInstance(Class<T> filterClass, Class<?> expectedType, Object argument) {
        try {
            Constructor<T>[] constructors = (Constructor<T>[]) filterClass.getDeclaredConstructors();
            Constructor<T> filterConstructor = findConstructor(constructors, Class.class, Object.class);

            if  (filterConstructor != null) {
                return filterConstructor.newInstance(expectedType, argument);
            } else {
                filterConstructor = findConstructor(constructors, Class.class);

                if  (filterConstructor != null) {
                    return filterConstructor.newInstance(expectedType);
                } else {
                    filterConstructor = findConstructor(constructors, Object.class);
                
                    if  (filterConstructor != null) {
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
    
    @Override
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, PaginatedCriteriaBuilder<?> criteriaBuilder) {
        return applyObjectBuilder(clazz, null, criteriaBuilder);
    }
    
    @Override
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, CriteriaBuilder<?> criteriaBuilder) {
        return applyObjectBuilder(clazz, null, criteriaBuilder);
    }
    
    @Override
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, PaginatedCriteriaBuilder<?> criteriaBuilder) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (QueryBuilder<?, ?>) criteriaBuilder);
        return (PaginatedCriteriaBuilder<T>) criteriaBuilder;
    }
    
    @Override
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, CriteriaBuilder<?> criteriaBuilder) {
        ViewType<T> viewType = getMetamodel().view(clazz);
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (QueryBuilder<?, ?>) criteriaBuilder);
        return (CriteriaBuilder<T>) criteriaBuilder;
    }

    @Override
    public <T> ObjectBuilderFactory<T> getObjectBuilderFactory(ViewType<T> viewType) {
        return new ObjectBuilderFactoryImpl<T>(getTemplate(viewType, null));
    }

    @Override
    public <T> ObjectBuilderFactory<T> getObjectBuilderFactory(MappingConstructor<T> mappingConstructor) {
        return new ObjectBuilderFactoryImpl<T>(getTemplate(mappingConstructor.getDeclaringType(), mappingConstructor));
    }
    
    private <T> void applyObjectBuilder(ViewType<T> viewType, MappingConstructor<T> mappingConstructor, QueryBuilder<?, ?> criteriaBuilder) {
        criteriaBuilder.selectNew(new ViewTypeObjectBuilderImpl<T>(getTemplate(viewType, mappingConstructor), criteriaBuilder));
    }
    
    private <T> ViewTypeObjectBuilderTemplate<T> getTemplate(ViewType<T> viewType, MappingConstructor<T> mappingConstructor) {
        ViewTypeObjectBuilderTemplate.Key<T> key = new ViewTypeObjectBuilderTemplate.Key<T>(viewType, mappingConstructor);
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

    private void registerFilterMappings() {
        filterMappings.put(ContainsFilter.class.getName(), ContainsFilterImpl.class);
        filterMappings.put(ContainsIgnoreCaseFilter.class.getName(), ContainsIgnoreCaseFilterImpl.class);
        filterMappings.put(StartsWithFilter.class.getName(), StartsWithFilterImpl.class);
        filterMappings.put(StartsWithIgnoreCaseFilter.class.getName(), StartsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(EndsWithFilter.class.getName(), EndsWithFilterImpl.class);
        filterMappings.put(EndsWithIgnoreCaseFilter.class.getName(), EndsWithIgnoreCaseFilterImpl.class);
        filterMappings.put(ExactFilter.class.getName(), ExactFilterImpl.class);
        filterMappings.put(NullFilter.class.getName(), NullFilterImpl.class);
        filterMappings.put(GreaterThanFilter.class.getName(), GreaterThanFilterImpl.class);
        filterMappings.put(GreaterOrEqualFilter.class.getName(), GreaterOrEqualFilterImpl.class);
        filterMappings.put(LessThanFilter.class.getName(), LessThanFilterImpl.class);
        filterMappings.put(LessOrEqualFilter.class.getName(), LessOrEqualFilterImpl.class);
    }
    
}
