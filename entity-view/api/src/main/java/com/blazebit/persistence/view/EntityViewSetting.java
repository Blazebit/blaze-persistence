package com.blazebit.persistence.view;

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
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
    private final Map<String, Filter> filters = new HashMap<String, Filter>();
    private final Map<String, Sorter> attributeSorters = new HashMap<String, Sorter>();
    private final Map<String, String> attributeFilters = new HashMap<String, String>();
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
        resolveAttributeFilters(viewType);
        resolveAttributeSorters(viewType);
        
        // Add filters
        if (!filters.isEmpty()) {
            for (Map.Entry<String, Filter> filterEntry : filters.entrySet()) {
                filterEntry.getValue().apply(cb, filterEntry.getKey());
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
    public void addAttributeSorter(String attributeName, Sorter sorter) {
        this.attributeSorters.put(attributeName, sorter);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public void addAttributeSorter(Map<String, Sorter> attributeSorters) {
        this.attributeSorters.putAll(attributeSorters);
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
    public Map<String, Filter> getFilters() {
        return filters;
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public Map<String, String> getAttributeFilters() {
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

    private void resolveAttributeSorters(ViewType<?> viewType) {
        Iterator<Map.Entry<String, Sorter>> iter = attributeSorters.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<String, Sorter> attributeSorterEntry = iter.next();
            String attributeName = attributeSorterEntry.getKey();
            sorters.put(viewType.getAttribute(attributeName).getMapping(), attributeSorterEntry.getValue());
            iter.remove();
        }
    }
    
    private void resolveAttributeFilters(ViewType<?> viewType) {
        Iterator<Map.Entry<String, String>> iter = attributeFilters.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<String, String> attributeFilterEntry = iter.next();
            String attributeName = attributeFilterEntry.getKey();
            String filterValue = attributeFilterEntry.getValue();
            Class<? extends Filter> filterClass = viewType.getAttribute(attributeName).getFilterMapping();
            if (filterClass == null) {
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName + "' in the entity view type '" + viewType.getJavaType().getName() + "'");
            }
            
            Filter filter = null;
            
            try {
                Constructor<?>[] constructors = filterClass.getDeclaredConstructors();
                Constructor<? extends Filter> filterConstructor = findConstructor(constructors, String.class);

                if (filterConstructor != null) {
                    filter = filterConstructor.newInstance(filterValue);
                } else {
                    filterConstructor = findConstructor(constructors, Object.class);

                    if  (filterConstructor != null) {
                        filter = filterConstructor.newInstance((Object) filterValue);
                    } else {
                        filterConstructor = findConstructor(constructors);

                        if (filterConstructor == null) {
                            throw new IllegalArgumentException("No suitable constructor found for filter class '" + filterClass.getName() + "'");
                        }

                        filter = filterConstructor.newInstance();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Could not create an instance of the filter class '" + filterClass.getName() + "'", ex);
            }
            
            filters.put(viewType.getAttribute(attributeName).getMapping(), filter);
            iter.remove();
        }
    }

    private Constructor<? extends Filter> findConstructor(Constructor<?>[] constructors, Class<?>... classes) {
        for (int i = 0; i < constructors.length; i++) {
            if (Arrays.equals(constructors[i].getParameterTypes(), classes)) {
                return (Constructor<? extends Filter>) constructors[i];
            }
        }
        
        return null;
    }
}
