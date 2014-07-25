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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.spi.ObjectBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.ViewMetamodelImpl;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
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
    
    public EntityViewManagerImpl(EntityViewConfigurationImpl config) {
        this.metamodel = new ViewMetamodelImpl(config.getEntityViews());
        this.proxyFactory = new ProxyFactory();
        this.objectBuilderCache = new ConcurrentHashMap<ViewTypeObjectBuilderTemplate.Key<?>, ViewTypeObjectBuilderTemplate<?>>();
    }

    @Override
    public ViewMetamodel getMetamodel() {
        return metamodel;
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
            value = key.createValue(proxyFactory);
            ViewTypeObjectBuilderTemplate<?> oldValue = objectBuilderCache.putIfAbsent(key, value);
            
            if (oldValue != null) {
                value = oldValue;
            }
        }
        
        return (ViewTypeObjectBuilderTemplate<T>) value;
    }
    
}
