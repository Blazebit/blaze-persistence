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

package com.blazebit.persistence.view;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;

/**
 *
 * @author cpbec
 */
public interface EntityViewManager {
    
    public EntityViewManagerFactory getEntityViewManagerFactory();
    
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, PaginatedCriteriaBuilder<?> criteriaBuilder);
    
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, CriteriaBuilder<?> criteriaBuilder);
    
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, PaginatedCriteriaBuilder<?> criteriaBuilder);
    
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, CriteriaBuilder<?> criteriaBuilder);
}
