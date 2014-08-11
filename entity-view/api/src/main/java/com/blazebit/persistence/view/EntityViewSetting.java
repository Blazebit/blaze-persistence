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

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * A {@linkplain EntityViewSetting} is a set of filters and sorters that can be applied to a {@link CriteriaBuilder}.
 * Filters and sorters are added for entity view attribute names. It also supports pagination and optional parameters.
 * Optional parameters are only set on a criteria builder if they are needed but not satisfied.
 *
 * @param <T> The type of the entity view
 * @param <Q> {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
 * @author Christian Beikov
 * @since 1.0
 */
public final class EntityViewSetting<T, Q extends QueryBuilder<T, Q>> {

    private final Class<T> entityViewClass;
    private final int firstResult;
    private final int maxResults;
    private final boolean paginate;
    private final Map<String, Sorter> sorters = new LinkedHashMap<String, Sorter>();
    private final Map<Object, Filter> filters = new HashMap<Object, Filter>();
    private final Map<String, Sorter> attributeSorters = new LinkedHashMap<String, Sorter>();
    private final Map<String, Object> attributeFilters = new HashMap<String, Object>();
    private final Map<String, Sorter> processedAttributeSorters = new LinkedHashMap<String, Sorter>();
    private final Map<String, Object> processedAttributeFilters = new HashMap<String, Object>();
    private final Map<String, Object> optionalParameters = new HashMap<String, Object>();

    private EntityViewSetting(Class<T> entityViewClass, int firstRow, int maxRows, boolean paginate) {
        this.entityViewClass = entityViewClass;
        this.firstResult = firstRow;
        this.maxResults = maxRows;
        this.paginate = paginate;
    }

    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on criteria builders.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, CriteriaBuilder<T>> create(Class<T> entityViewClass) {
        return new EntityViewSetting<T, CriteriaBuilder<T>>(entityViewClass, 0, Integer.MAX_VALUE, false);
    }

    /**
     * Creates a new {@linkplain EntityViewSetting} that can be applied on criteria builders.
     *
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param firstRow        The position of the first result to retrieve, numbered from 0
     * @param maxRows         The maximum number of results to retrieve
     * @param <T>             The type of the entity view
     * @return A new entity view setting
     */
    public static <T> EntityViewSetting<T, PaginatedCriteriaBuilder<T>> create(Class<T> entityViewClass, int firstRow, int maxRows) {
        return new EntityViewSetting<T, PaginatedCriteriaBuilder<T>>(entityViewClass, firstRow, maxRows, true);
    }

    /**
     * Applies this entity view setting to the given criteria builder.
     *
     * @param entityViewManager The entity view manager that manages the entity view of this setting
     * @param criteriaBuilder   The criteria builder on which the setting should be applied
     * @return {@linkplain PaginatedCriteriaBuilder} if paginated, {@linkplain CriteriaBuilder} otherwise
     */
    public Q apply(EntityViewManager entityViewManager, CriteriaBuilder<?> criteriaBuilder) {
        resolveAttributeSorters(entityViewManager, criteriaBuilder.getMetamodel());
        resolveAttributeFilters(entityViewManager, criteriaBuilder.getMetamodel());

        // Add filters
        if (!filters.isEmpty()) {
            for (Map.Entry<Object, Filter> filterEntry : filters.entrySet()) {
                Object key = filterEntry.getKey();

                if (key instanceof SubqueryAttribute<?, ?>) {
                    SubqueryAttribute<?, ?> subqueryAttribute = (SubqueryAttribute<?, ?>) key;
                    SubqueryProvider provider;

                    try {
                        provider = subqueryAttribute.getSubqueryProvider()
                            .newInstance();
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Could not instantiate the subquery provider: " + subqueryAttribute
                            .getSubqueryProvider()
                            .getName(), ex);
                    }

                    if (subqueryAttribute.getSubqueryExpression()
                        .isEmpty()) {
                        filterEntry.getValue()
                            .apply(provider.createSubquery(criteriaBuilder.where()));
                    } else {
                        // TODO: apply expression and alias
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }
                } else {
                    filterEntry.getValue()
                        .apply(criteriaBuilder.where((String) key));
                }
            }
        }

        // Add sorters
        if (!sorters.isEmpty()) {
            for (Map.Entry<String, Sorter> sorterEntry : sorters.entrySet()) {
                sorterEntry.getValue()
                    .apply(criteriaBuilder, sorterEntry.getKey());
            }
        }

        CriteriaBuilder<T> normalCb = entityViewManager.applyObjectBuilder(entityViewClass, criteriaBuilder);
        PaginatedCriteriaBuilder<T> paginatedCb = null;

        if (paginate) {
            paginatedCb = normalCb.page(firstResult, maxResults);
        }

        // Add optional parameters
        if (!optionalParameters.isEmpty()) {
            for (Map.Entry<String, Object> paramEntry : optionalParameters.entrySet()) {
                if (normalCb.containsParameter(paramEntry.getKey()) && !normalCb.isParameterSet(paramEntry.getKey())) {
                    normalCb.setParameter(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        }

        if (paginatedCb != null) {
            return (Q) paginatedCb;
        } else {
            return (Q) criteriaBuilder;
        }
    }

    /**
     * Adds the given attribute filters to the attribute filters of this setting.
     *
     * @param attributeFilters The attribute filters to add
     */
    public void addAttributeFilters(Map<String, String> attributeFilters) {
        this.attributeFilters.putAll(attributeFilters);
    }

    /**
     * Adds the given attribute filter to the attribute filters of this setting.
     *
     * @param attributeName The name of the attribute filter
     * @param filterValue   The filter value for the attribute filter
     */
    public void addAttributeFilter(String attributeName, String filterValue) {
        this.attributeFilters.put(attributeName, filterValue);
    }

    /**
     * Adds the given attribute sorters to the attribute sorters of this setting.
     * Note that the attribute sorter order is retained.
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
     * Adds the given optional parameters to the optional parameters of this setting.
     *
     * @param optionalParameters The optional parameters to add
     */
    public void addOptionalParameters(Map<String, Object> optionalParameters) {
        this.optionalParameters.putAll(optionalParameters);
    }

    /**
     * Adds the given optional parameter to the optional parameters of this setting.
     *
     * @param parameterName The name of the optional parameter
     * @param value         The value of the optional parameter
     */
    public void addOptionalParameter(String parameterName, Object value) {
        this.optionalParameters.put(parameterName, value);
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
     * The first result that the criteria builder should return. Returns 0 if no pagination will be applied.
     *
     * @see QueryBuilder#page(int, int)
     * @return The first result
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * The maximum number of results that the criteria builder should return. Returns {@linkplain java.lang.Integer#MAX_VALUE}
     * if no pagination will be applied.
     *
     * @see QueryBuilder#page(int, int)
     * @return The maximum number of results
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Returns true if sorters have been added, otherwise false.
     *
     * @return true if sorters have been added, otherwise false
     */
    public boolean hasSorters() {
        return !attributeSorters.isEmpty() || !sorters.isEmpty();
    }

    /**
     * Returns a copy of the attribute sorters that have been added.
     *
     * @return The attribute sorters
     */
    public Map<String, Sorter> getAttributeSorters() {
        Map<String, Sorter> newAttributeSorters = new HashMap<String, Sorter>(attributeSorters);
        newAttributeSorters.putAll(processedAttributeSorters);
        return newAttributeSorters;
    }

    /**
     * Returns true if filters have been added, otherwise false.
     *
     * @return true if filters have been added, otherwise false
     */
    public boolean hasFilters() {
        return !attributeFilters.isEmpty() || !filters.isEmpty();
    }

    /**
     * Returns a copy of the attribute filters that have been added.
     *
     * @return The attribute filters
     */
    public Map<String, Object> getAttributeFilters() {
        Map<String, Object> newAttributeFilters = new HashMap<String, Object>(attributeFilters);
        newAttributeFilters.putAll(processedAttributeFilters);
        return newAttributeFilters;
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
        return new HashMap<String, Object>(optionalParameters);
    }

    private void resolveAttributeSorters(EntityViewManager evm, Metamodel jpaMetamodel) {
        ViewMetamodel metamodel = evm.getMetamodel();
        ViewType<T> viewType = metamodel.view(entityViewClass);
        Iterator<Map.Entry<String, Sorter>> iter = attributeSorters.entrySet()
            .iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Sorter> attributeSorterEntry = iter.next();
            String attributeName = attributeSorterEntry.getKey();
            Sorter sorter = attributeSorterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);
            String mapping;

            if (attributeInfo.entityAttribute) {
                mapping = attributeInfo.mapping.toString();
            } else {
                mapping = resolveAttributeAlias(viewType, attributeSorterEntry.getKey());
            }

            sorters.put(mapping, sorter);
            processedAttributeSorters.put(attributeName, sorter);
            iter.remove();
        }
    }

    private void resolveAttributeFilters(EntityViewManager evm, Metamodel jpaMetamodel) {
        ViewMetamodel metamodel = evm.getMetamodel();
        ViewType<T> viewType = metamodel.view(entityViewClass);
        Iterator<Map.Entry<String, Object>> iter = attributeFilters.entrySet()
            .iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Object> attributeFilterEntry = iter.next();
            String attributeName = attributeFilterEntry.getKey();
            Object filterValue = attributeFilterEntry.getValue();
            AttributeInfo attributeInfo = resolveAttributeInfo(metamodel, jpaMetamodel, viewType, attributeName);

            Class<? extends Filter> filterClass;
            Class<?> expectedType;

            if (attributeInfo.entityAttribute) {
                // No filters available
                filterClass = null;
                expectedType = null;
            } else {
                MethodAttribute<?, ?> attribute = attributeInfo.attribute;
                filterClass = attribute.getFilterMapping();
                expectedType = attribute.getJavaType();
            }

            if (filterClass == null) {
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName
                    + "' in the entity view type '" + viewType.getJavaType()
                    .getName() + "'");
            }

            Filter filter = evm.createFilter(filterClass, expectedType, filterValue);
            filters.put(attributeInfo.mapping, filter);
            processedAttributeFilters.put(attributeName, filterValue);
            iter.remove();
        }
    }

    private String resolveAttributeAlias(ViewType<?> viewType, String attributeName) {
        String viewTypeName = viewType.getName();
        StringBuilder sb = new StringBuilder(viewTypeName.length() + attributeName.length() + 1);
        sb.append(viewTypeName)
            .append('_');

        for (int i = 0; i < attributeName.length(); i++) {
            char c = attributeName.charAt(i);

            if (c == '.') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private AttributeInfo resolveAttributeInfo(ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType, String attributePath) {
        if (attributePath.indexOf('.') == -1) {
            MethodAttribute<?, ?> attribute = viewType.getAttribute(attributePath);
            Object mapping;

            if (attribute.isSubquery()) {
                mapping = attribute;
            } else {
                mapping = ((MappingAttribute<?, ?>) attribute).getMapping();
            }

            return new AttributeInfo(attribute, null, mapping, false);
        }

        String[] parts = attributePath.split("\\.");
        ViewType<?> currentViewType = viewType;
        MethodAttribute<?, ?> currentAttribute = null;
        StringBuilder sb = new StringBuilder();
        Object mapping = null;
        Attribute<?, ?> jpaAttribute = null;
        boolean foundEntityAttribute = false;

        for (int i = 0; i < parts.length; i++) {
            currentAttribute = currentViewType.getAttribute(parts[i]);

            if (currentAttribute.isSubquery()) {
                // Note that if a subquery filtering is done, we ignore the mappings we gathered in the StringBuilder
                mapping = currentAttribute;

                if (i + 1 != parts.length) {
                    // Since subqueries can't return objects, it makes no sense to further navigate
                    throw new IllegalArgumentException("The given attribute path '" + attributePath
                        + "' is accessing the property '" + parts[i + 1] + "' of a subquery attribute in the type '"
                        + currentAttribute.getJavaType()
                        .getName() + "' which is illegal!");
                }
            } else if (currentAttribute.isSubview()) {
                if (i != 0) {
                    sb.append('.');
                }

                sb.append(((MappingAttribute<?, ?>) currentAttribute).getMapping());

                if (currentAttribute.isCollection()) {
                    PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) currentAttribute;
                    currentViewType = metamodel.view(pluralAttribute.getElementType());
                } else {
                    currentViewType = metamodel.view(currentAttribute.getJavaType());
                }
            } else if (i + 1 != parts.length) {
                Class<?> maybeUnmanagedType = currentAttribute.getJavaType();
                i++;

                try {
                    ManagedType<?> type;
                    currentAttribute = null;
                    foundEntityAttribute = true;
                    // If we get here, the attribute type is a managed type and we can copy the rest of the parts
                    for (; i < parts.length; i++) {
                        type = jpaMetamodel.managedType(maybeUnmanagedType);
                        sb.append('.');
                        sb.append(parts[i]);
                        jpaAttribute = type.getAttribute(parts[i]);
                        maybeUnmanagedType = jpaAttribute.getJavaType();
                    }

                    break;
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The given attribute path '" + attributePath
                        + "' is accessing the possibly unknown property '" + parts[i] + "' of the type '" + maybeUnmanagedType
                        .getName() + "' which is illegal!", ex);
                }
            } else {
                if (i != 0) {
                    sb.append('.');
                }

                sb.append(((MappingAttribute<?, ?>) currentAttribute).getMapping());
            }
        }

        if (mapping == null) {
            mapping = sb.toString();
        }

        return new AttributeInfo(currentAttribute, jpaAttribute, mapping, foundEntityAttribute);
    }

    private static class AttributeInfo {

        private final MethodAttribute<?, ?> attribute;
        private final Attribute<?, ?> jpaAttribute;
        private final Object mapping;
        private final boolean entityAttribute;

        public AttributeInfo(MethodAttribute<?, ?> attribute, Attribute<?, ?> jpaAttribute, Object mapping, boolean entityAttribute) {
            this.attribute = attribute;
            this.jpaAttribute = jpaAttribute;
            this.mapping = mapping;
            this.entityAttribute = entityAttribute;
        }

    }
}
