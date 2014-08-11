package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.MappingFilter;


/**
 * A placeholder for a filter implementation that implements a is null and is not null filter.
 * This placeholder can be used in a {@link MappingFilter} annotation or you can retrieve an instance of this filter by invoking
 * {@link EntityViewManager#createFilter(java.lang.Class, java.lang.Class, java.lang.Object)}.
 *
 * A null filter accepts an object. The {@linkplain Object#toString()} representation of that object will be parsed to a boolean
 * if the object is not instance of {@linkplain Boolean}.
 * If the resulting boolean is true, the filter will apply an is null restriction, otherwise an is not null restriction.
 * 
 * @author Christian Beikov
 * @since 1.0
 */
public interface NullFilter extends Filter {
    
}
