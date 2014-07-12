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
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewManagerFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewType;
import javax.persistence.EntityManager;

/**
 *
 * @author cpbec
 */
public class EntityViewManagerImpl implements EntityViewManager {
    
    private final EntityViewManagerFactory entityViewManagerFactory;
    private final EntityManager em;

    public EntityViewManagerImpl(EntityViewManagerFactory entityViewManagerFactory, EntityManager em) {
        this.entityViewManagerFactory = entityViewManagerFactory;
        this.em = em;
    }

    @Override
    public EntityViewManagerFactory getEntityViewManagerFactory() {
        return entityViewManagerFactory;
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
        ViewType<T> viewType = entityViewManagerFactory.getMetamodel().view(clazz);
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (QueryBuilder<?, ?>) criteriaBuilder);
        return (PaginatedCriteriaBuilder<T>) criteriaBuilder;
    }
    
    @Override
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, CriteriaBuilder<?> criteriaBuilder) {
        ViewType<T> viewType = entityViewManagerFactory.getMetamodel().view(clazz);
        MappingConstructor<T> mappingConstructor = viewType.getConstructor(mappingConstructorName);
        applyObjectBuilder(viewType, mappingConstructor, (QueryBuilder<?, ?>) criteriaBuilder);
        return (CriteriaBuilder<T>) criteriaBuilder;
    }
    
    private <T> void applyObjectBuilder(ViewType<T> viewType, MappingConstructor<T> mappingConstructor, QueryBuilder<?, ?> criteriaBuilder) {
        criteriaBuilder.selectNew(new ViewTypeObjectBuilderImpl<T>(viewType, mappingConstructor));
    }
}
