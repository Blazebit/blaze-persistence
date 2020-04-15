/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * A placeholder for a filter implementation that implements a starts with filter.
 * This placeholder can be used in a {@link AttributeFilter} annotation.
 *
 * A starts with filter accepts an object. The {@linkplain Object#toString()} representation of that object will be used as value
 * for the starts with restriction.
 *
 * @param <FilterValue> The type of the filter value i.e. the attribute type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class StartsWithFilter<FilterValue> extends AttributeFilterProvider<FilterValue> {

}
