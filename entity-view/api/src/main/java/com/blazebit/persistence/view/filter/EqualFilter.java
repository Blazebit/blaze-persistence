package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.MappingFilter;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * A placeholder for a filter implementation that implements an equal filter.
 * This placeholder can be used in a {@link MappingFilter} annotation or you can retrieve an instance of this filter by invoking
 * {@link EntityViewManager#createFilter(java.lang.Class, java.lang.Class, java.lang.Object)}.
 *
 * An equal filter accepts a class and an object. The class is interpreted as the expected type. This is used to convert the
 * object parameter. The following conversion are done based on the expected type in the right order.
 *
 * <ul>
 * <li>If the value is a {@link SubqueryProvider}, the filter will create a subquery restriction.</li>
 * <li>If the value is an instance of the expected type, the value will be used in the restriction as is.</li>
 * <li>If the parsing of the {@linkplain Object#toString()} representation of the object to the expected type is successful,
 * the parsed value will be used in the restriction.</li>
 * <li>If the parsing of the object fails, the value will be used in the restriction as is.</li>
 * </ul>
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface EqualFilter extends Filter {

}
