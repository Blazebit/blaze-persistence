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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.LockMode;

import java.util.Set;

/**
 * Represents the metamodel of an entity view.
 *
 * @param <X> The type of the entity view
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ViewType<X> extends ManagedViewType<X> {

    /**
     * Returns the name of the entity view.
     *
     * @return The name of the entity view.
     */
    public String getName();

    /**
     * Returns the id attribute of the entity view.
     *
     * @return The id attribute of the entity view
     */
    public MethodAttribute<? super X, ?> getIdAttribute();

    /**
     * Returns the version attribute of the entity view if there is any, or null.
     *
     * @return The version attribute of the entity view, or null
     * @since 1.2.0
     */
    public MethodAttribute<? super X, ?> getVersionAttribute();

    /**
     * Returns path to the lock owner relative from the view types entity class if there is any, or null.
     *
     * @return The path to the lock owner, or null
     * @since 1.2.0
     */
    public String getLockOwner();

    /**
     * Returns the lock mode that is used for this entity view, or null.
     *
     * @return The lock mode, or null
     * @since 1.2.0
     */
    public LockMode getLockMode();
    
    /**
     * Returns the view filter mapping of the entity view with the given name.
     * 
     * @param filterName The name of the view filter mapping which should be returned
     * @return The view filter mapping of the entity view with the given name
     */
    public ViewFilterMapping getViewFilter(String filterName);
    
    /**
     * Returns the view filter mappings of the entity view.
     * 
     * @return The view filter mappings of the entity view
     */
    public Set<ViewFilterMapping> getViewFilters();
}
