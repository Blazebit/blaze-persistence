/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * A placeholder for a filter implementation that implements a between filter.
 * This placeholder can be used in a {@link AttributeFilter} annotation.
 *
 * A between filter accepts an object array or a {@link Range}.
 *
 * @param <FilterValue> The type of the filter value i.e. the attribute type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class BetweenFilter<FilterValue> extends AttributeFilterProvider<Range<FilterValue>> {
}
