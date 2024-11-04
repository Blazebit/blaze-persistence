/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Specifies the behavior of a collection mapping. This can be used to configure ordering, uniqueness and the sort order.
 *
 * The default sort order of {@link SortedSet} and {@link SortedMap} is the natural order. By using this annotation, the sort order can be overridden.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionMapping {
    
    /**
     * The comparator that should be used for sorting of the collection.
     *
     * @return the comparator used for sorting
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Comparator> comparator() default Comparator.class;
    
    /**
     * Specifies whether entries in the collection should be ordered.
     *
     * @return true if ordered, false otherwise
     */
    boolean ordered() default false;
    
    /**
     * Specifies whether the index of the indexed collection should be ignored or not.
     *
     * @return true if the index should be ignored, false otherwise
     */
    boolean ignoreIndex() default false;

    /**
     * Specifies whether the elements should be forcefully deduplicated if the collection allows duplicates or not.
     *
     * @return true if the elements should be forcefully deduplicated, false otherwise
     */
    boolean forceUnique() default false;
}
