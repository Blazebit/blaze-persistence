/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * A placeholder for a filter implementation that implements a ends with filter that is not case sensitive.
 * This placeholder can be used in a {@link AttributeFilter} annotation.
 *
 * An ends with ignore case filter accepts an object. The {@linkplain Object#toString()} representation of that object will be
 * used as value for the ends with restriction.
 *
 * @param <FilterValue> The type of the filter value i.e. the attribute type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class EndsWithIgnoreCaseFilter<FilterValue> extends AttributeFilterProvider<FilterValue> {

}
