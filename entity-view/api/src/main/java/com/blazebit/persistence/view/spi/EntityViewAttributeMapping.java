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

package com.blazebit.persistence.view.spi;

import java.util.Comparator;

/**
 * Mapping of an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewAttributeMapping {

    /**
     * Returns the mapping of the view declaring this attribute.
     *
     * @return The declaring view mapping
     */
    public EntityViewMapping getDeclaringView();

    /**
     * Returns whether this attribute is of the plural type.
     *
     * @return <code>true</code> if this attribute is of the plural type, <code>false</code> otherwise
     */
    public boolean isCollection();

    /**
     * Returns the behavior of a plural attribute container if the attribute is plural, or <code>null</code> otherwise.
     *
     * @return The container behavior, or null if attribute is singular
     */
    public ContainerBehavior getContainerBehavior();

    /**
     * Sets the container behavior to {@link ContainerBehavior#DEFAULT}.
     */
    public void setContainerDefault();

    /**
     * Sets the container behavior to {@link ContainerBehavior#INDEXED}.
     */
    public void setContainerIndexed();

    /**
     * Sets the container behavior to {@link ContainerBehavior#ORDERED}.
     */
    public void setContainerOrdered();

    /**
     * Sets the container behavior to {@link ContainerBehavior#SORTED} using the given
     * comparator class, if given, for sorting. If none is given, the key/element type
     * is expected to implement {@link Comparable}.
     *
     * @param comparatorClass The class of the comparator to use for sorting or <code>null</code>
     */
    public void setContainerSorted(Class<? extends Comparator<?>> comparatorClass);

    /**
     * Returns the comparator class, or <code>null</code> if there none.
     *
     * @return The comparator class
     */
    public Class<? extends Comparator<?>> getComparatorClass();

    /**
     * Returns the default batch size to use for batched {@link com.blazebit.persistence.view.FetchStrategy#SELECT} fetching.
     *
     * @return The default batch size
     */
    public Integer getDefaultBatchSize();

    /**
     * Sets the default batch size to use for batched {@link com.blazebit.persistence.view.FetchStrategy#SELECT} fetching.
     *
     * @param defaultBatchSize The default batch size
     */
    public void setDefaultBatchSize(Integer defaultBatchSize);

    /**
     * Returns the attribute type.
     *
     * @return The attribute type
     */
    public Class<?> getDeclaredType();

    /**
     * The attribute's key type, or <code>null</code> if the attribute type is not a subtype of {@link java.util.Map}.
     *
     * @return The attribute's key type, or <code>null</code>
     */
    public Class<?> getDeclaredKeyType();

    /**
     * The attribute's element type, or <code>null</code> if the attribute type is not a subtype of {@link java.util.Collection} or {@link java.util.Map}.
     *
     * @return The attribute's element type, or <code>null</code>
     */
    public Class<?> getDeclaredElementType();

    /**
     * The behavior of a plural attribute container.
     */
    public static enum ContainerBehavior {
        /**
         * The default behavior doesn't mandate a deterministic ordering.
         */
        DEFAULT,
        /**
         * Specifies that the container's iteration order must match the element insertion order.
         */
        ORDERED,
        /**
         * Specifies that the elements of the container are indexed upon which the iteration order is based on.
         */
        INDEXED,
        /**
         * Specifies that the container's iteration order must match the sort order as defined by a comparator or a comparable element type.
         */
        SORTED;
    }
}
