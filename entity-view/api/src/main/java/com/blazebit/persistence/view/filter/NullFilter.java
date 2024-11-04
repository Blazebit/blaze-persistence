/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * A placeholder for a filter implementation that implements a is null and is not null filter.
 * This placeholder can be used in a {@link AttributeFilter} annotation.
 *
 * A null filter accepts an object. The {@linkplain Object#toString()} representation of that object will be parsed to a boolean
 * if the object is not instance of {@linkplain Boolean}.
 * If the resulting boolean is true, the filter will apply an is null restriction, otherwise an is not null restriction.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class NullFilter extends AttributeFilterProvider<Object> {

}
