/*
 * Copyright 2014 - 2021 Blazebit.
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

/**
 * A filter mapping for an attribute path.
 *
 * @param <X> The type of the entity view that is the base of the path
 * @param <FilterValue> The filter value type
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class AttributeFilterMappingPath<X, FilterValue> {

    private final AttributePath<X, ?, ?> attributePath;
    private final AttributeFilterMapping<?, FilterValue> filter;
    private final String filterName;

    /**
     * Creates a new attribute filter mapping path.
     *
     * @param attributePath The attribute path
     * @param filter The filter mapping
     */
    public AttributeFilterMappingPath(AttributePath<X, ?, ?> attributePath, AttributeFilterMapping<?, FilterValue> filter) {
        this.attributePath = attributePath;
        this.filter = filter;
        this.filterName = filter.getName();
    }

    /**
     * Creates a new attribute filter mapping path.
     *
     * @param attributePath The attribute path
     * @param filterName The filter name
     */
    public AttributeFilterMappingPath(AttributePath<X, ?, ?> attributePath, String filterName) {
        this.attributePath = attributePath;
        this.filter = null;
        this.filterName = filterName;
    }

    /**
     * Returns the attribute path.
     *
     * @return The attribute path
     */
    public AttributePath<X, ?, ?> getAttributePath() {
        return attributePath;
    }

    /**
     * Returns the filter mapping.
     *
     * @return the filter mapping
     */
    public AttributeFilterMapping<?, FilterValue> getFilter() {
        return filter;
    }

    /**
     * Returns the filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return filterName;
    }
}
