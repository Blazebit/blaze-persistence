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

/**
 * Instances of the type {@linkplain PluralAttribute} represent collection-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <C> The type of the represented collection
 * @param <E> The element type of the represented collection
 * @author Christian Beikov
 * @since 1.0
 */
public interface PluralAttribute<X, C, E> extends MappingAttribute<X, C> {

    /**
     * Returns the collection type.
     *
     * @return The collection type
     */
    public CollectionType getCollectionType();

    /**
     * Returns the type representing the element type of the collection.
     *
     * @return The element type
     */
    public Class<E> getElementType();

    /**
     * The different collection types.
     */
    public static enum CollectionType {

        /**
         * Collection-valued attribute.
         */
        COLLECTION,
        /**
         * List-valued attribute.
         */
        LIST,
        /**
         * Map-valued attribute.
         */
        MAP,
        /**
         * Set-valued attribute.
         */
        SET;
    }
}
