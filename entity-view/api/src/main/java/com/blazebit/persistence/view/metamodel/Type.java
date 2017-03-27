/*
 * Copyright 2014 - 2017 Blazebit.
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

/**
 * Represents the mapping type of a view or attribute.
 *
 * @param <X> The type of the view or attribute
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Type<X> {

    /**
     * Returns the java class of the type.
     *
     * @return the java class
     */
    public Class<X> getJavaType();

    /**
     * Returns the mapping type.
     *
     * @return The mapping type
     */
    public MappingType getMappingType();

    /**
     * The different mapping types.
     */
    public static enum MappingType {

        /**
         * Basic type.
         */
        BASIC,
        /**
         * Flat view type.
         */
        FLAT_VIEW,
        /**
         * View type.
         */
        VIEW;
    }
}
