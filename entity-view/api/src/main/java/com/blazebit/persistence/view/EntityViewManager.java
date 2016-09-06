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

import javax.persistence.EntityManager;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;

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
     * Updates the entity which the given entity view maps to.
     * Issues a partial update if enabled for the given view.
     * 
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @since 1.1.0
     */
    public void update(EntityManager entityManager, Object view);
    
    /**
     * Fully updates the entity which the given entity view maps to.
     * 
     * @param entityManager The entity manager to use for the update
     * @param view The view to use for updating
     * @since 1.1.0
     */
    public void updateFull(EntityManager entityManager, Object view);

    /**
     * Applies the entity view setting to the given criteria builder.
     *
     * @param setting         The setting that should be applied
     * @param criteriaBuilder The criteria builder on which the setting should be applied
     * @param <T>             The type of the entity view
     * @param <Q>             {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
     * @return {@linkplain PaginatedCriteriaBuilder} if paginated,
     *         {@linkplain CriteriaBuilder} otherwise
     */
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder);

    /**
     * Applies the entity view setting to the given entity view root of the criteria builder.
     *
     * @param setting         The setting that should be applied
     * @param criteriaBuilder The criteria builder on which the setting should be applied
     * @param entityViewRoot  The relation from which the entity view should be materialized
     * @param <T>             The type of the entity view
     * @param <Q>             {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
     * @return {@linkplain PaginatedCriteriaBuilder} if paginated,
     *         {@linkplain CriteriaBuilder} otherwise
     * @since 1.2.0
     */
    public <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder, String entityViewRoot);
}
