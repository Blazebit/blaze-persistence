/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.Filter;

/**
 * Represents an attribute of a view type.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0
 */
public interface MappingAttribute<X, Y> {

    /**
     * Returns the declaring view type.
     *
     * @return The declaring view type
     */
    public ViewType<X> getDeclaringType();

    /**
     * Returns the java type of the attribute.
     *
     * @return The java type of the attribute
     */
    public Class<Y> getJavaType();

    /**
     * Returns the mapping of the attribute.
     *
     * @return The mapping of the attribtue
     */
    public String getMapping();

    /**
     * Returns true if this attribute maps to a query parameter, otherwise false.
     *
     * @return True if this attribute maps to a query parameter, otherwise false
     */
    public boolean isMappingParameter();
}
