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

package com.blazebit.persistence.view;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;

import java.util.*;

/**
 * A {@linkplain EntityViewSetting} is a set of filters and sorters that can be
 * applied to a {@link CriteriaBuilder}. Filters and sorters are added for
 * entity view attribute names. It also supports pagination and optional
 * parameters. Optional parameters are only set on a criteria builder if they
 * are needed but not satisfied.
 *
 * @param <T> The type of the entity view
 * @param <Q> {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public final class EntityViewSetting<T, Q extends FullQueryBuilder<T, Q>> {

    private final Class<T> entityViewClass;
    private final String viewConstructorName;
    private final Object entityId;
    private final int firstResult;
    private final int maxResults;
    private final boolean paginated;
    private final Set<String> viewNamedFilters;
    private final Map<String, Sorter> attributeSorters;
    private final Map<String, AttributeFilterActivation> attributeFilters;
    private final Map<String, Object> optionalParameters;
    private final Map<String, Object> properties;
    
    private KeysetPage keysetPage;
    private boolean keysetPaginated;

    private EntityViewSetting(Class<T> entityViewClass, Object entityId, int maxResults, boolean paginate, String viewConstructorName) {
        this.entityViewClass = entityViewClass;
        this.viewConstructorName = viewConstructorName;
        this.entityId = entityId;
        this.firstResult = -1;
        this.maxResults = maxResults;
        this.paginated = paginate;
        this.viewNamedFilters = new LinkedHashSet<>();
        this.attributeSorters = new LinkedHashMap<>();
        this.attributeFilters = new LinkedHashMap<>();
        this.optionalParameters = new HashMap<>();
        this.properties = new HashMap<>();
    }

    private EntityViewSetting(Class<T> entityViewClass, int firstResult, int maxResults, boolean paginate, String viewConstructorName) {
        if (firstResult < 0) {
            throw new IllegalArgumentException("Invalid negative value for firstResult");
        }
        
        this.entityViewClass = entityViewClass;
        this.viewConstructorName = viewConstructorName;
        this.entityId = null;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.paginated = paginate;
        this.viewNamedFilters = new LinkedHashSet<>();
        this.attributeSorters = new LinkedHashMap<>();
        this.attributeFilters = new LinkedHashMap<>();
        this.optionalParameters = new HashMap<>();
        this.properties = new HashMap<>();
    }

    private EntityViewSetting(EntityViewSetting<? super T, ?> original, Class<T> subtype) {
        this.entityViewClass = subtype;
        this.viewConstructorName = original.viewConstructorName;
        this.entityId = original.entityId;
        this.firstResult = original.firstResult;
        this.maxResults = original.maxResults;
        this.paginated = original.paginated;
        this.keysetPage = original.keysetPage;
        this.keysetPaginated = original.keysetPaginated;
        this.viewNamedFilters = new LinkedHashSet<>(original.viewNamedFilters);
        this.attributeSorters = new LinkedHashMap<>(original.attributeSorters);
        this.attributeFilters = new LinkedHashMap<>(original.attributeFilters);
        this.optionalParameters = new HashMap<>(original.optionalParameters);
        this.properties = new HashMap<>(original.properties);
    }

    /**
     * Like {@link EntityViewSetting#create(java.lang.Class, java.lang.String)} but with the <code>viewConstructorname</code> set to null.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, CriteriaBuilder<T>> create(Class<T> entityViewClass) {
        return new EntityViewSetting<T, CriteriaBuilder<T>>(entityViewClass, 0, Integer.MAX_VALUE, false, null);
    }
    
    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on
     * criteria builders.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param viewConstructorName The name of the view constructor
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, CriteriaBuilder<T>> create(Class<T> entityViewClass, String viewConstructorName) {
        return new EntityViewSetting<T, CriteriaBuilder<T>>(entityViewClass, 0, Integer.MAX_VALUE, false, viewConstructorName);
    }

    /**
     * Like {@link EntityViewSetting#create(java.lang.Class, int, int, java.lang.String)} but with the <code>viewConstructorname</code> set to null.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param firstResult     The position of the first result to retrieve, numbered from 0
     * @param maxResults      The maximum number of results to retrieve
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, int firstResult, int maxResults) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, firstResult, maxResults, true, null);
    }
    
    /**
     * Like {@link EntityViewSetting#create(java.lang.Class, java.lang.Object, int, java.lang.String)} but with the <code>viewConstructorname</code> set to null.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param entityId        The id of the entity which should be located on a page
     * @param maxResults      The maximum number of results to retrieve
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, Object entityId, int maxResults) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, entityId, maxResults, true, null);
    }
    
    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on
     * criteria builders.
     *
     * @param entityViewClass     The entity view class that should be used for the object builder
     * @param firstResult         The position of the first result to retrieve, numbered from 0
     * @param maxResults          The maximum number of results to retrieve
     * @param viewConstructorName The name of the view constructor
     * @param <T>                 The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, int firstResult, int maxResults, String viewConstructorName) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, firstResult, maxResults, true, viewConstructorName);
    }
    
    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on
     * criteria builders.
     *
     * @param entityViewClass     The entity view class that should be used for the object builder
     * @param entityId            The id of the entity which should be located on a page
     * @param maxResults          The maximum number of results to retrieve
     * @param viewConstructorName The name of the view constructor
     * @param <T>                 The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, Object entityId, int maxResults, String viewConstructorName) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, entityId, maxResults, true, viewConstructorName);
    }

    /**
     * Creates a copy of <code>this</code> {@linkplain EntityViewSetting} for the given entity view subtype.
     *
     * @param subtype The entity view subtype
     * @param <X> Entity view subtype
     * @param <Y> The query builder type
     * @return A copy for the given subtype
     */
    public <X extends T, Y extends FullQueryBuilder<X, Y>> EntityViewSetting<X, Y> forSubtype(Class<X> subtype) {
        return new EntityViewSetting<>(this, subtype);
    }

    /**
     * Returns the entity view class.
     *
     * @return The entity view class
     */
    public Class<T> getEntityViewClass() {
        return entityViewClass;
    }

    /**
     * Returns the entity view constructor name.
     * 
     * @return The entity view constructor name
     */
    public String getViewConstructorName() {
        return viewConstructorName;
    }

    /**
     * The id of the entity which should be located on the page returned result.
     * Returns <code>null</code> if no pagination or a absolute first result will be applied.
     *
     * @return The id of the entity which should be located on a page
     * @see FullQueryBuilder#page(java.lang.Object, int)
     */
    public Object getEntityId() {
        return entityId;
    }

    /**
     * The first result that the criteria builder should return. Returns 0 if no
     * pagination will be applied. Returns -1 if an entity id was supplied.
     *
     * @return The first result
     * @see FullQueryBuilder#page(int, int)
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * The maximum number of results that the criteria builder should return.
     * Returns {@linkplain java.lang.Integer#MAX_VALUE} if no pagination will be
     * applied.
     *
     * @return The maximum number of results
     * @see FullQueryBuilder#page(int, int)
     */
    public int getMaxResults() {
        return maxResults;
    }
    
    /**
     * Returns true if this entiy view setting applies pagination, false otherwise.
     * 
     * @return True if this entiy view setting applies pagination, false otherwise
     */
    public boolean isPaginated() {
        return paginated;
    }

    /**
     * Returns the key set of this setting.
     * 
     * @return The key set of this setting
     */
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    /**
     * Sets the key set of this setting.
     * 
     * @param keysetPage the new key set
     * @return this setting for chaining
     */
    public EntityViewSetting<T, Q> withKeysetPage(KeysetPage keysetPage) {
        this.keysetPage = keysetPage;
        this.keysetPaginated = true;
        return this;
    }

    /**
     * Returns true if this setting is key set paginated.
     * 
     * @return true if this setting is key set paginated
     */
    public boolean isKeysetPaginated() {
        return keysetPaginated;
    }

    /**
     * Adds the given attribute sorters to the attribute sorters of this
     * setting. Note that the attribute sorter order is retained.
     *
     * @param attributeSorters The attribute sorters to add
     */
    public void addAttributeSorters(Map<String, Sorter> attributeSorters) {
        this.attributeSorters.putAll(attributeSorters);
    }

    /**
     * Adds the given attribute sorter to the attribute sorters of this setting.
     * Note that the attribute sorter order is retained.
     *
     * @param attributeName The name of the attribute sorter
     * @param sorter        The sorter for the attribute sorter
     */
    public void addAttributeSorter(String attributeName, Sorter sorter) {
        this.attributeSorters.put(attributeName, sorter);
    }

    /**
     * Adds the given attribute sorter to the attribute sorters of this setting.
     * Note that the attribute sorter order is retained.
     *
     * @param attributeName The name of the attribute sorter
     * @param sorter        The sorter for the attribute sorter
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withAttributeSorter(String attributeName, Sorter sorter) {
        addAttributeSorter(attributeName, sorter);
        return this;
    }

    /**
     * Returns true if sorters have been added, otherwise false.
     *
     * @return true if sorters have been added, otherwise false
     */
    public boolean hasAttributeSorters() {
        return !attributeSorters.isEmpty();
    }

    /**
     * Returns a copy of the attribute sorters that have been added.
     *
     * @return The attribute sorters
     */
    public Map<String, Sorter> getAttributeSorters() {
        return attributeSorters;
    }

    /**
     * Adds the given attribute filters to the attribute filters of this
     * setting.
     *
     * @param attributeFilters The attribute filters to add
     */
    public void addAttributeFilters(Map<String, Object> attributeFilters) {
        for (Map.Entry<String, Object> attributeFilterEntry : attributeFilters.entrySet()) {
            addAttributeFilter(attributeFilterEntry.getKey(), attributeFilterEntry.getValue());
        }
    }

    /**
     * Adds the attribute's default attribute filter to the attribute filters of this setting
     * or overwrites the filter value of an existing default attribute filter.
     *
     * @param attributeName The name of the attribute filter
     * @param filterValue   The filter value for the attribute filter
     */
    public void addAttributeFilter(String attributeName, Object filterValue) {
        checkExistingFiltersForAttribute(attributeName, AttributeFilter.DEFAULT_NAME);
        this.attributeFilters.put(attributeName, new AttributeFilterActivation(filterValue));
    }

    /**
     * Adds the attribute's default attribute filter to the attribute filters of this setting
     * or overwrites the filter value of an existing default attribute filter.
     *
     * @param attributeName The name of the attribute filter
     * @param filterValue   The filter value for the attribute filter
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withAttributeFilter(String attributeName, Object filterValue) {
        addAttributeFilter(attributeName, filterValue);
        return this;
    }

    /**
     * Adds the attribute's attribute filter with the given name to the attribute filters of this setting
     * or overwrites the filter value of an existing attribute filter with the same attribute name and filter name.
     *
     * @param attributeName The attribute name
     * @param filterName    The filter name
     * @param filterValue   The filter value for the attribute filter
     */
    public void addAttributeFilter(String attributeName, String filterName, Object filterValue) {
        checkExistingFiltersForAttribute(attributeName, filterName);
        this.attributeFilters.put(attributeName, new AttributeFilterActivation(filterName, filterValue));
    }

    /**
     * Adds the attribute's attribute filter with the given name to the attribute filters of this setting
     * or overwrites the filter value of an existing attribute filter with the same attribute name and filter name.
     *
     * @param attributeName The attribute name
     * @param filterName    The filter name
     * @param filterValue   The filter value for the attribute filter
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withAttributeFilter(String attributeName, String filterName, Object filterValue) {
        addAttributeFilter(attributeName, filterName, filterValue);
        return this;
    }

    private void checkExistingFiltersForAttribute(String attributeName, String attributeFilterName) {
        AttributeFilterActivation attributeFilterActivation = this.attributeFilters.get(attributeName);
        if (attributeFilterActivation != null) {
            if (!attributeFilterActivation.getAttributeFilterName().equals(attributeFilterName)) {
                throw new IllegalArgumentException("At most one active attribute filter per attribute is allowed! attributeName = '" + attributeName + "'");
            }
        }
    }

    /**
     * Returns true if filters have been added, otherwise false.
     *
     * @return true if filters have been added, otherwise false
     */
    public boolean hasAttributeFilters() {
        return !attributeFilters.isEmpty() ;
    }

    /**
     * Returns a copy of the attribute filters that have been added.
     *
     * @return The attribute filters
     */
    public Map<String, AttributeFilterActivation> getAttributeFilters() {
        return attributeFilters;
    }
    
    /**
     * Enables and adds the view filter with the given name in this setting.
     *
     * @param filterName The name of the view filter
     */
    public void addViewFilter(String filterName) {
        this.viewNamedFilters.add(filterName);
    }

    /**
     * Enables and adds the view filter with the given name in this setting.
     *
     * @param filterName The name of the view filter
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withViewFilter(String filterName) {
        addViewFilter(filterName);
        return this;
    }

    /**
     * Returns true if named filters for the view have been added, otherwise false.
     *
     * @return true if named filters for the view have been added, otherwise false
     */
    public boolean hasViewFilters() {
        return !viewNamedFilters.isEmpty();
    }

    /**
     * Returns a copy of the named filters for the view that have been added.
     *
     * @return The named filters for the view
     */
    public Set<String> getViewFilters() {
        return viewNamedFilters;
    }

    /**
     * Adds the given optional parameters to the optional parameters of this
     * setting.
     *
     * @param optionalParameters The optional parameters to add
     */
    public void addOptionalParameters(Map<String, Object> optionalParameters) {
        this.optionalParameters.putAll(optionalParameters);
    }

    /**
     * Adds the given optional parameter to the optional parameters of this
     * setting.
     *
     * @param parameterName The name of the optional parameter
     * @param value         The value of the optional parameter
     */
    public void addOptionalParameter(String parameterName, Object value) {
        this.optionalParameters.put(parameterName, value);
    }

    /**
     * Adds the given optional parameter to the optional parameters of this
     * setting.
     *
     * @param parameterName The name of the optional parameter
     * @param value         The value of the optional parameter
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withOptionalParameter(String parameterName, Object value) {
        addOptionalParameter(parameterName, value);
        return this;
    }

    /**
     * Returns true if optional parameters have been added, otherwise false.
     *
     * @return true if optional parameters have been added, otherwise false
     */
    public boolean hasOptionalParameters() {
        return !optionalParameters.isEmpty();
    }

    /**
     * Returns a copy of the optional parameters that have been added.
     *
     * @return The optional parameters
     */
    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }

    /**
     * Set a entity view property or hint.
     * If a property or hint is not recognized, it is silently ignored.
     * @param propertyName name of property or hint
     * @param value  value for property or hint
     * @since 1.2.0
     */
    public void setProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
    }

    /**
     * Set a entity view property or hint.
     * If a property or hint is not recognized, it is silently ignored.
     * @param propertyName name of property or hint
     * @param value  value for property or hint
     * @return <code>this</code> for method chaining
     * @since 1.3.0
     */
    public EntityViewSetting<T, Q> withProperty(String propertyName, Object value) {
        setProperty(propertyName, value);
        return this;
    }

    /**
     * Get the properties and hints and associated values that are in effect
     * for the entity view setting.
     * @return map of properties and hints in effect for entity view stting
     * @since 1.2.0
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * @author Moritz Becker
     * @since 1.2.0
     */
    public static class AttributeFilterActivation {
        private final String attributeFilterName;
        private final Object filterValue;

        private AttributeFilterActivation(Object filterValue) {
            this(AttributeFilter.DEFAULT_NAME, filterValue);
        }

        private AttributeFilterActivation(String attributeFilterName, Object filterValue) {
            this.attributeFilterName = attributeFilterName;
            this.filterValue = filterValue;
        }

        public String getAttributeFilterName() {
            return attributeFilterName;
        }

        public Object getFilterValue() {
            return filterValue;
        }
    }
}
