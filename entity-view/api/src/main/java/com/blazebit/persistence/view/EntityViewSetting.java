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
 * @since 1.0
 */
public final class EntityViewSetting<T, Q extends FullQueryBuilder<T, Q>> {

    private final Class<T> entityViewClass;
    private final String viewConstructorName;
    private final Object entityId;
    private final int firstResult;
    private final int maxResults;
    private final boolean paginated;
    private final Set<String> viewNamedFilters = new LinkedHashSet<String>();
    private final Set<String> attributeNamedFilters = new LinkedHashSet<String>();
    private final Map<String, Sorter> attributeSorters = new LinkedHashMap<String, Sorter>();
    private final Map<String, Object> attributeFilters = new LinkedHashMap<String, Object>();
    private final Map<String, Object> optionalParameters = new HashMap<String, Object>();
    private final Map<String, Object> properties = new HashMap<String, Object>();
    
    private KeysetPage keysetPage;
    private boolean keysetPaginated;

    private EntityViewSetting(Class<T> entityViewClass, Object entityId, int maxRows, boolean paginate, String viewConstructorName) {
        this.entityViewClass = entityViewClass;
        this.viewConstructorName = viewConstructorName;
        this.entityId = entityId;
        this.firstResult = -1;
        this.maxResults = maxRows;
        this.paginated = paginate;
    }

    private EntityViewSetting(Class<T> entityViewClass, int firstRow, int maxRows, boolean paginate, String viewConstructorName) {
        if (firstRow < 0) {
            throw new IllegalArgumentException("Invalid negative value for firstRow");
        }
        
        this.entityViewClass = entityViewClass;
        this.viewConstructorName = viewConstructorName;
        this.entityId = null;
        this.firstResult = firstRow;
        this.maxResults = maxRows;
        this.paginated = paginate;
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
     * @param firstRow        The position of the first result to retrieve, numbered from 0
     * @param maxRows         The maximum number of results to retrieve
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, int firstRow, int maxRows) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, firstRow, maxRows, true, null);
    }
    
    /**
     * Like {@link EntityViewSetting#create(java.lang.Class, java.lang.Object, int, java.lang.String)} but with the <code>viewConstructorname</code> set to null.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param entityId        The id of the entity which should be located on a page
     * @param maxRows         The maximum number of results to retrieve
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, Object entityId, int maxRows) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, entityId, maxRows, true, null);
    }
    
    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on
     * criteria builders.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param firstRow        The position of the first result to retrieve, numbered from 0
     * @param maxRows         The maximum number of results to retrieve
     * @param viewConstructorName The name of the view constructor
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, int firstRow, int maxRows, String viewConstructorName) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, firstRow, maxRows, true, viewConstructorName);
    }
    
    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on
     * criteria builders.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param entityId        The id of the entity which should be located on a page
     * @param maxRows         The maximum number of results to retrieve
     * @param viewConstructorName The name of the view constructor
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, Object entityId, int maxRows, String viewConstructorName) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, entityId, maxRows, true, viewConstructorName);
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
     * @see FullQueryBuilder#page(java.lang.Object, int)
     * @return The id of the entity which should be located on a page
     */
    public Object getEntityId() {
        return entityId;
    }

    /**
     * The first result that the criteria builder should return. Returns 0 if no
     * pagination will be applied. Returns -1 if an entity id was supplied.
     *
     * @see FullQueryBuilder#page(int, int)
     * @return The first result
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * The maximum number of results that the criteria builder should return.
     * Returns {@linkplain java.lang.Integer#MAX_VALUE} if no pagination will be
     * applied.
     *
     * @see FullQueryBuilder#page(int, int)
     * @return The maximum number of results
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
        this.attributeFilters.putAll(attributeFilters);
    }

    /**
     * Adds the given attribute filter to the attribute filters of this setting.
     *
     * @param attributeName The name of the attribute filter
     * @param filterValue   The filter value for the attribute filter
     */
    public void addAttributeFilter(String attributeName, Object filterValue) {
        this.attributeFilters.put(attributeName, filterValue);
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
    public Map<String, Object> getAttributeFilters() {
        return attributeFilters;
    }
    
    /**
     * Enables and adds the attribute filter with the given name in this setting.
     *
     * @param filterName The name of the attribute filter
     */
    public void addAttributeNamedFilter(String filterName) {
        this.attributeNamedFilters.add(filterName);
    }

    /**
     * Returns true if named filters for attributes have been added, otherwise false.
     *
     * @return true if named filters for attributes have been added, otherwise false
     */
    public boolean hasAttributeNamedFilters() {
        return !attributeNamedFilters.isEmpty();
    }

    /**
     * Returns a copy of the named filters for attributes that have been added.
     *
     * @return The named filters for attributes
     */
    public Set<String> getAttributeNamedFilters() {
        return attributeNamedFilters;
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
     * @throws IllegalArgumentException if the second argument is
     *         not valid for the implementation
     * @since 1.2.0
     */
    public void setProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
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
}
