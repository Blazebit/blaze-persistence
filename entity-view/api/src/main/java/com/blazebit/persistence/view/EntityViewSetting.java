package com.blazebit.persistence.view;

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.util.Iterator;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * TODO: javadoc
 *
 * @param <T> The type of the entity view
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewSetting<T> {

    private final Class<T> entityViewClass;
    private final int firstRow;
    private final int maxRows;
    private final Map<String, Sorter> sorters = new HashMap<String, Sorter>();
    private final Map<Object, Filter> filters = new HashMap<Object, Filter>();
    private final Map<String, Sorter> attributeSorters = new HashMap<String, Sorter>();
    private final Map<String, Object> attributeFilters = new HashMap<String, Object>();
    private final Map<String, Object> optionalParameters = new HashMap<String, Object>();
    
    /**
     * Constructs a new {@linkplain EntityViewSetting} that can be applied on criteria builders.
     * 
     * @param entityViewClass The entity view class that should be used for the object builder
     * @param firstRow The position of the first result to retrieve, numbered from 0
     * @param maxRows The maximum number of results to retrieve
     */
    public EntityViewSetting(Class<T> entityViewClass, int firstRow, int maxRows) {
        this.entityViewClass = entityViewClass;
        this.firstRow = firstRow;
        this.maxRows = maxRows;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public PaginatedCriteriaBuilder<T> apply(EntityViewManager evm, CriteriaBuilder<?> cb) {
        resolveAttributeSorters(evm, cb.getMetamodel());
        resolveAttributeFilters(evm, cb.getMetamodel());
        
        // Add filters
        if (!filters.isEmpty()) {
            for (Map.Entry<Object, Filter> filterEntry : filters.entrySet()) {
                Object key = filterEntry.getKey();
                
                if (key instanceof Class) {
                    Class<? extends SubqueryProvider> subqueryProviderClass = (Class<? extends SubqueryProvider>) key;
                    SubqueryProvider provider;
                    
                    try {
                        provider = subqueryProviderClass.newInstance();
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Could not instantiate the subquery provider: " + subqueryProviderClass.getName(), ex);
                    }
                    
                    filterEntry.getValue().apply(provider.createSubquery(cb.where()));
                } else {
                    filterEntry.getValue().apply(cb.where((String) key));
                }
            }
        }

        // Add sorters
        if (!sorters.isEmpty()) {
            for (Map.Entry<String, Sorter> sorterEntry : sorters.entrySet()) {
                sorterEntry.getValue().apply(cb, sorterEntry.getKey());
            }
        }
        
        PaginatedCriteriaBuilder<T> paginatedCb = evm.applyObjectBuilder(entityViewClass, cb)
                .page(firstRow, maxRows);
        
        // Add optional parameters
        if (!optionalParameters.isEmpty()) {
            for (Map.Entry<String, Object> paramEntry : optionalParameters.entrySet()) {
                if (paginatedCb.containsParameter(paramEntry.getKey()) && !paginatedCb.isParameterSet(paramEntry.getKey())) {
                    paginatedCb.setParameter(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        }
        
        return paginatedCb;
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addAttributeFilters(Map<String, String> attributeFilters) {
        this.attributeFilters.putAll(attributeFilters);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addAttributeFilter(String attributeName, String filterValue) {
        this.attributeFilters.put(attributeName, filterValue);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addFilters(String expression, Filter filter) {
        this.filters.put(expression, filter);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addFilters(Map<String, Filter> filters) {
        this.filters.putAll(filters);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addAttributeSorters(Map<String, Sorter> attributeSorters) {
        this.attributeSorters.putAll(attributeSorters);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addAttributeSorter(String attributeName, Sorter sorter) {
        this.attributeSorters.put(attributeName, sorter);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addSorter(String expression, Sorter sorter) {
        this.sorters.put(expression, sorter);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addSorters(Map<String, Sorter> sorters) {
        this.sorters.putAll(sorters);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addOptionalParameter(String parameterName, Object value) {
        this.optionalParameters.put(parameterName, value);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addOptionalParameters(Map<String, Object> optionalParameters) {
        this.optionalParameters.putAll(optionalParameters);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Class<T> getEntityViewClass() {
        return entityViewClass;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public int getMaxRows() {
        return maxRows;
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public boolean hasSorters() {
        return !attributeSorters.isEmpty() || !sorters.isEmpty();
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<String, Sorter> getSorters() {
        return sorters;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<String, Sorter> getAttributeSorters() {
        return attributeSorters;
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public boolean hasFilters() {
        return !attributeFilters.isEmpty() || !filters.isEmpty();
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<Object, Filter> getFilters() {
        return filters;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<String, Object> getAttributeFilters() {
        return attributeFilters;
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public boolean hasOptionalParameters() {
        return !optionalParameters.isEmpty();
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }
    
    private void resolveAttributeSorters(EntityViewManager evm, Metamodel jpaMetamodel) {
        ViewMetamodel metamodel = evm.getMetamodel();
        ViewType<T> viewType = metamodel.view(entityViewClass);
        Iterator<Map.Entry<String, Sorter>> iter = attributeSorters.entrySet().iterator();
        
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
            iter.remove();
        }
    }
    
    private void resolveAttributeFilters(EntityViewManager evm, Metamodel jpaMetamodel) {
        ViewMetamodel metamodel = evm.getMetamodel();
        ViewType<T> viewType = metamodel.view(entityViewClass);
        Iterator<Map.Entry<String, Object>> iter = attributeFilters.entrySet().iterator();
        
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
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName + "' in the entity view type '" + viewType.getJavaType().getName() + "'");
            }
            
            Filter filter = evm.createFilter(filterClass, expectedType, filterValue);
            filters.put(attributeInfo.mapping, filter);
            
            iter.remove();
        }
    }

    private String resolveAttributeAlias(ViewType<?> viewType, String attributeName) {
        String viewTypeName = viewType.getName();
        StringBuilder sb = new StringBuilder(viewTypeName.length() + attributeName.length() + 1);
        sb.append(viewTypeName).append('_');
        
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

    private AttributeInfo resolveAttributeInfo(ViewMetamodel metamodel, Metamodel jpaMetamodel, ViewType<?> viewType, String attributePath) {
        if (attributePath.indexOf('.') == -1) {
            MethodAttribute<?, ?> attribute = viewType.getAttribute(attributePath);
            Object mapping;
            
            if (attribute.isSubquery()) {
                mapping = ((SubqueryAttribute<?, ?>) attribute).getSubqueryProvider();
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
                mapping = ((SubqueryAttribute<?, ?>) currentAttribute).getSubqueryProvider();
                
                if (i + 1 != parts.length) {
                    // Since subqueries can't return objects, it makes no sense to further navigate
                    throw new IllegalArgumentException("The given attribute path '" + attributePath + "' is accessing the property '" + parts[i + 1] + "' of a subquery attribute in the type '" + currentAttribute.getJavaType().getName() + "' which is illegal!");
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
                    throw new IllegalArgumentException("The given attribute path '" + attributePath + "' is accessing the possibly unknown property '" + parts[i] + "' of the type '" + maybeUnmanagedType.getName() + "' which is illegal!", ex);
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
}
