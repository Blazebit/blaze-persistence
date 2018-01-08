/*
 * Copyright 2014 - 2018 Blazebit.
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
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * A placeholder for a filter implementation that implements a greater or equal filter.
 * This placeholder can be used in a {@link AttributeFilter} annotation.
 *
 * A greater or equal filter accepts a class and an object. The class is interpreted as the expected type. This is used to convert the
 * object parameter. The following conversion are done based on the expected type in the right order.
 *
 * <ul>
 * <li>If the value is a {@link SubqueryProvider}, the filter will create a subquery restriction.</li>
 * <li>If the value is an instance of the expected type, the value will be used in the restriction as is.</li>
 * <li>If the parsing of the {@linkplain Object#toString()} representation of the object to the expected type is successful,
 * the parsed value will be used in the restriction.</li>
 * <li>If the parsing of the object fails, an {@link IllegalArgumentException} is thrown.</li>
 * </ul>
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class GreaterOrEqualFilter extends AttributeFilterProvider {

}
