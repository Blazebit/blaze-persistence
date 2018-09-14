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

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Specifies the comparator that should be used for sorting.
 * This annotation is only valid on {@linkplain SortedSet}, {@linkplain NavigableSet}, {@linkplain NavigableMap} and {@linkplain SortedMap}.
 *
 * The default sort order is the natural order. By using this annotation, the sort order will be overridden.
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
     * @return true if the elements should be forcefully depduplicated, false otherwise
     */
    boolean forceUnique() default false;
}
