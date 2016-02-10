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
package com.blazebit.persistence.view.metamodel;

import java.util.Set;

/**
 * Represents the metamodel of an entity view.
 *
 * @param <X> The type of the entity view
 * @author Christian Beikov
 * @since 1.0
 */
public interface ViewType<X> extends IdentifiableViewType<X> {

    /**
     * Returns the name of the entity view.
     *
     * @return The name of the entity view.
     */
    public String getName();
    
    /**
     * Returns whether the entity view is updatable.
     * 
     * @return Whether the entity view is updatable
     */
    public boolean isUpdatable();

    /**
     * Returns whether the entity view is partially updatable.
     * 
     * @return Whether the entity view is partially updatable
     */
    public boolean isPartiallyUpdatable();
    
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
