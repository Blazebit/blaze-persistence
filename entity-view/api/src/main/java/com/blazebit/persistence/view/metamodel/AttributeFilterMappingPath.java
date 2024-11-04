/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
