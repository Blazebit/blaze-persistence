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
import com.blazebit.persistence.spi.ObjectBuilderFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 * An interface that gives access to the metamodel and object builders.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface EntityViewManager {

    /**
     * Returns the metamodel for this entity view manager.
     *
     * @return The metamodel for this entity view manager
     */
    public ViewMetamodel getMetamodel();
    
    /**
     * TODO: javadoc
     * 
     * @return 
     */
    public <T> ObjectBuilderFactory<T> getObjectBuilderFactory(ViewType<T> viewType);
    
    /**
     * TODO: javadoc
     * 
     * @return 
     */
    public <T> ObjectBuilderFactory<T> getObjectBuilderFactory(MappingConstructor<T> mappingConstructor);

    /**
     * Applies an object builder for the given entity view class to the given {@link PaginatedCriteriaBuilder}
     *
     * @param <T>             The type of the entity view class
     * @param clazz           The entity view class
     * @param criteriaBuilder The criteria build on which the object build should be applied to
     * @return A paginated criteria builder with an object builder applied
     */
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, PaginatedCriteriaBuilder<?> criteriaBuilder);

    /**
     * Applies an object builder for the given entity view class to the given {@link CriteriaBuilder}
     *
     * @param <T>             The type of the entity view class
     * @param clazz           The entity view class
     * @param criteriaBuilder The criteria build on which the object build should be applied to
     * @return A paginated criteria builder with an object builder applied
     */
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, CriteriaBuilder<?> criteriaBuilder);

    /**
     * Applies an object builder for the given entity view class and the given mapping constructor name to the given {@link PaginatedCriteriaBuilder}
     *
     * @param <T>                    The type of the entity view class
     * @param clazz                  The entity view class
     * @param mappingConstructorName The name of the constructor that should be used in the object builder
     * @param criteriaBuilder        The criteria build on which the object build should be applied to
     * @return A paginated criteria builder with an object builder applied
     */
    public <T> PaginatedCriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, PaginatedCriteriaBuilder<?> criteriaBuilder);

    /**
     * Applies an object builder for the given entity view class and the given mapping constructor name to the given {@link CriteriaBuilder}
     *
     * @param <T>                    The type of the entity view class
     * @param clazz                  The entity view class
     * @param mappingConstructorName The name of the constructor that should be used in the object builder
     * @param criteriaBuilder        The criteria build on which the object build should be applied to
     * @return A paginated criteria builder with an object builder applied
     */
    public <T> CriteriaBuilder<T> applyObjectBuilder(Class<T> clazz, String mappingConstructorName, CriteriaBuilder<?> criteriaBuilder);
}
