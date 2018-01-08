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

import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.ViewFilterProvider;

/**
 * Represents the mapping of a named filter.
 *
 * @param <T> The base filter type which is either {@linkplain AttributeFilterProvider} or {@linkplain ViewFilterProvider}. 
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface FilterMapping<T> {
    
    /**
     * Returns the name of the filter.
     * 
     * @return The name of the filter
     */
    public String getName();
    
    /**
     * Returns true if this is a {@link ViewFilterMapping}, false if this is a {@link AttributeFilterMapping}.
     * 
     * @return True if this is a {@link ViewFilterMapping}, false if this is a {@link AttributeFilterMapping}
     */
    public boolean isViewFilter();
    
    /**
     * Returns the filter class.
     * 
     * @return The filter class
     */
    public Class<? extends T> getFilterClass();
}
