/*
 * Copyright 2014 - 2022 Blazebit.
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


import com.blazebit.persistence.spi.ServiceProvider;

/**
 * Represents an attribute that has a mapping expression.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MappingAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the mapping of the attribute.
     *
     * @return The mapping of the attribute
     */
    public String getMapping();

    /**
     * Renders the mapping for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb);
}
