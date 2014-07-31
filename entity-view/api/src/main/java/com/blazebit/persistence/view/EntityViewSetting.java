package com.blazebit.persistence.view;

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.util.Iterator;

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
        ViewType<T> viewType = evm.getMetamodel().view(entityViewClass);
        resolveAttributeFilters(evm, viewType);
        
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
    public void addFilters(Map<String, Filter> filters) {
        this.filters.putAll(filters);
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
        return !sorters.isEmpty();
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
    
    private void resolveAttributeFilters(EntityViewManager evm, ViewType<?> viewType) {
        Iterator<Map.Entry<String, Object>> iter = attributeFilters.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<String, Object> attributeFilterEntry = iter.next();
            String attributeName = attributeFilterEntry.getKey();
            Object filterValue = attributeFilterEntry.getValue();
            MethodAttribute<?, ?> attribute = viewType.getAttribute(attributeName);
            Class<? extends Filter> filterClass = attribute.getFilterMapping();
            if (filterClass == null) {
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName + "' in the entity view type '" + viewType.getJavaType().getName() + "'");
            }
            
            Filter filter = evm.createFilter(filterClass, attribute.getJavaType(), filterValue);
            
            if (attribute.isSubquery()) {
                filters.put(((SubqueryAttribute<?, ?>) attribute).getSubqueryProvider(), filter);
            } else {
                filters.put(((MappingAttribute<?, ?>) attribute).getMapping(), filter);
            }
            iter.remove();
        }
    }
}
