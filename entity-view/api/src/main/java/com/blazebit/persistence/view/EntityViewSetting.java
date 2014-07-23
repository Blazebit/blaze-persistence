package com.blazebit.persistence.view;

import java.util.HashMap;
import java.util.Map;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;

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
    public void addFilters(Map<String, Filter> filters) {
        this.filters.putAll(filters);
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
    public Map<String, Sorter> getSorters() {
        return sorters;
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
    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }
}
