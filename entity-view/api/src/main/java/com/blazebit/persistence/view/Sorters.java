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

import com.blazebit.persistence.OrderByBuilder;

/**
 * A utility class that provides methods to create standard sorters.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class Sorters {

    private Sorters() {
    }

    /**
     * Creates and returns a new {@link Sorter}. The sorter has the following properties
     * <ul>
     * <li>sorts ascending if the flag <code>ascending</code> is true, descending otherwise</li>
     * <li>sorts nulls first is the flag <code>nullsFirst</code> is ture, nulls last otherwise</li>
     * </ul>
     *
     * @param ascending  If true sorts ascending, otherwise descending
     * @param nullsFirst If true sorts nulls first, otherwise nulls last
     * @return A new sorter
     */
    public static Sorter sorter(boolean ascending, boolean nullsFirst) {
        return new DefaultSorter(ascending, nullsFirst);
    }

    /**
     * Like {@link Sorters#sorter(boolean, boolean)} but with <code>ascending</code> set to true.
     *
     * @param nullsFirst If true sorts nulls first, otherwise nulls last
     * @return A new sorter
     */
    public static Sorter ascending(boolean nullsFirst) {
        return new DefaultSorter(true, nullsFirst);
    }

    /**
     * Like {@link Sorters#sorter(boolean, boolean)} but with <code>ascending</code> set to false.
     *
     * @param nullsFirst If true sorts nulls first, otherwise nulls last
     * @return A new sorter
     */
    public static Sorter descending(boolean nullsFirst) {
        return new DefaultSorter(false, nullsFirst);
    }

    /**
     * Like {@link Sorters#ascending(boolean)} but with <code>nullsFirst</code> set to false.
     *
     * @return A new sorter
     */
    public static Sorter ascending() {
        return new DefaultSorter(true, false);
    }

    /**
     * Like {@link Sorters#descending(boolean)} but with <code>nullsFirst</code> set to false.
     *
     * @return A new sorter
     */
    public static Sorter descending() {
        return new DefaultSorter(false, false);
    }

    /**
     * The default sorter implementation.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class DefaultSorter implements Sorter {

        private final boolean ascending;
        private final boolean nullFirst;

        DefaultSorter(boolean ascending, boolean nullFirst) {
            this.ascending = ascending;
            this.nullFirst = nullFirst;
        }

        @Override
        public <T extends OrderByBuilder<T>> T apply(T sortable, String expression) {
            return sortable.orderBy(expression, ascending, nullFirst);
        }

    }
}
