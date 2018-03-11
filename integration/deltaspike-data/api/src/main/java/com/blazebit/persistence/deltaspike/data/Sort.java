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

package com.blazebit.persistence.deltaspike.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable container for sorting information, heavily inspired by Spring Data's <code>Sort</code>.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class Sort implements Iterable<Sort.Order>, Serializable {

    private final List<Order> orders;

    /**
     * Creates a new {@linkplain Sort} with the given {@link Order}s.
     *
     * @param orders The orders to use for sorting
     */
    public Sort(Order... orders) {
        this(Collections.unmodifiableList(Arrays.asList(orders)));
    }

    /**
     * Creates a new {@linkplain Sort} with the given {@link Order}s.
     *
     * @param orders The orders to use for sorting
     */
    public Sort(Collection<Order> orders) {
        this(Collections.unmodifiableList(new ArrayList<>(orders)));
    }

    /**
     * Creates a new {@linkplain Sort} with the given paths and the {@link Direction#ASC}.
     *
     * @param paths The paths by which to sort
     */
    public Sort(String... paths) {
        this(Direction.ASC, paths);
    }

    /**
     * Creates a new {@linkplain Sort} with the given paths the given direction.
     *
     * @param direction The direction to use for sorting
     * @param paths     The paths by which to sort
     */
    public Sort(Direction direction, String... paths) {
        this(direction, paths == null ? Collections.<String>emptyList() : Arrays.asList(paths));
    }

    /**
     * Creates a new {@linkplain Sort} with the given paths and given direction.
     *
     * @param direction The direction to use for sorting
     * @param paths     The paths by which to sort
     */
    public Sort(Direction direction, List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty paths!");
        }

        List<Order> orders = new ArrayList<>(paths.size());

        for (String property : paths) {
            orders.add(new Order(direction, property));
        }
        this.orders = Collections.unmodifiableList(orders);
    }

    private Sort(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty orders!");
        }

        this.orders = orders;
    }

    /**
     * Returns a new {@linkplain Sort} representing the union of the {@link Order}s of this and the given {@link Sort}.
     *
     * @param sort The other sort, may be null
     * @return A new {@linkplain Sort} with the union of this and the {@link Order}s of the given {@link Sort}
     */
    public Sort and(Sort sort) {
        if (sort == null) {
            return this;
        }

        List<Order> orders = new ArrayList<>(this.orders.size() + sort.orders.size());
        orders.addAll(this.orders);
        orders.addAll(sort.orders);
        return new Sort(Collections.unmodifiableList(orders));
    }

    /**
     * Returns the order for the given path or <code>null</code> if there is none.
     *
     * @param path The path for which to get the {@link Order}
     * @return The {@link Order} for the given path or <code>null</code> it there is none
     */
    public Order getOrderFor(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        final int size = orders.size();
        for (int i = 0; i < size; i++) {
            if (path.equals(orders.get(i).getPath())) {
                return orders.get(i);
            }
        }

        return null;
    }

    @Override
    public Iterator<Order> iterator() {
        return orders.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sort)) {
            return false;
        }

        Sort that = (Sort) obj;
        return this.orders.equals(that.orders);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (orders.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final int size = orders.size();
        orders.get(0).appendTo(sb);

        for (int i = 1; i < size; i++) {
            sb.append(',');
            orders.get(i).appendTo(sb);
        }

        return sb.toString();
    }

    /**
     * Sort directions.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum Direction {

        /**
         * Ascending direction.
         */
        ASC,
        /**
         * Descending direction.
         */
        DESC;

        /**
         * Returns whether the direction is ascending.
         *
         * @return <code>true</code> if ascending, <code>false</code> otherwise
         */
        public boolean isAscending() {
            return this == ASC;
        }

        /**
         * Returns whether the direction is descending.
         *
         * @return <code>true</code> if descending, <code>false</code> otherwise
         */
        public boolean isDescending() {
            return this == DESC;
        }

        /**
         * Returns the {@link Direction} enum for the given {@link String} value.
         *
         * @param value The direction string
         * @return The direction
         */
        public static Direction fromString(String value) {
            if ("asc".equalsIgnoreCase(value)) {
                return ASC;
            } else if ("desc".equalsIgnoreCase(value)) {
                return DESC;
            } else {
                throw new IllegalArgumentException("Illegal unknown direction: " + value);
            }
        }

        /**
         * Returns the {@link Direction} enum for the given {@link String} or <code>null</code> if the value is unknown.
         *
         * @param value The direction string
         * @return The direction or <code>null</code> if unknown
         */
        public static Direction fromStringOrNull(String value) {
            if ("asc".equalsIgnoreCase(value)) {
                return ASC;
            } else if ("desc".equalsIgnoreCase(value)) {
                return DESC;
            } else {
                return null;
            }
        }
    }

    /**
     * Enumeration for null handling of an {@link Order}.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum NullHandling {

        /**
         * Specifies that the underlying default null handling should be applied.
         */
        NATIVE,

        /**
         * Specifies that <code>NULL</code>s are ordered before other values.
         */
        NULLS_FIRST,

        /**
         * Specifies that <code>NULL</code>s are ordered after other values.
         */
        NULLS_LAST;
    }

    /**
     * PropertyPath implements the pairing of an {@link Direction} and a property. It is used to provide input for
     * {@link Sort}
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class Order implements Serializable {

        private final Direction direction;
        private final String path;
        private final boolean ignoreCase;
        private final NullHandling nullHandling;

        /**
         * Creates a new {@link Order} with the given direction and path.
         *
         * @param direction The {@link Direction} to use, defaults to {@link Direction#ASC} if <code>null</code>
         * @param path      The path to sort by
         */
        public Order(Direction direction, String path) {
            this(direction, path, false, null);
        }

        /**
         * Creates a new {@link Order} with the given direction, path and null handling.
         *
         * @param direction    The {@link Direction} to use, defaults to {@link Direction#ASC} if <code>null</code>
         * @param path         The path to sort by
         * @param nullHandling The {@link NullHandling} to use, defaults to {@link NullHandling#NATIVE} if <code>null</code>
         */
        public Order(Direction direction, String path, NullHandling nullHandling) {
            this(direction, path, false, nullHandling);
        }

        /**
         * Creates a new {@link Order} with the given path and {@link Direction#ASC}.
         *
         * @param path The path to sort by
         */
        public Order(String path) {
            this(Direction.ASC, path);
        }

        /**
         * Creates a new {@link Order} with the given direction, path, case sensitivity configuration and null handling.
         *
         * @param direction    The {@link Direction} to use, defaults to {@link Direction#ASC} if <code>null</code>
         * @param path         The path to sort by
         * @param ignoreCase   Whether sorting should be case insensitive, or not
         * @param nullHandling The {@link NullHandling} to use, defaults to {@link NullHandling#NATIVE} if <code>null</code>
         */
        private Order(Direction direction, String path, boolean ignoreCase, NullHandling nullHandling) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Property must not null or empty!");
            }

            this.direction = direction == null ? Direction.ASC : direction;
            this.path = path;
            this.ignoreCase = ignoreCase;
            this.nullHandling = nullHandling == null ? NullHandling.NATIVE : nullHandling;
        }

        /**
         * Returns the sort direction.
         *
         * @return The sort direction
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Returns the path of the {@linkplain Order}.
         *
         * @return The path to sort by
         */
        public String getPath() {
            return path;
        }

        /**
         * Returns whether sorting for this property should be ascending.
         *
         * @return <code>true</code> if sorting is ascending, <code>false</code> otherwise
         */
        public boolean isAscending() {
            return direction == Direction.ASC;
        }

        /**
         * Returns whether sorting for this path should be descending.
         *
         * @return <code>true</code> if sorting is descending, <code>false</code> otherwise
         */
        public boolean isDescending() {
            return direction == Direction.DESC;
        }

        /**
         * Returns whether or not the sort will be case sensitive.
         *
         * @return <code>true</code> if case insensitive, <code>false</code> otherwise
         */
        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        /**
         * Returns a new {@link Order} with the given {@link Direction}.
         *
         * @param direction The {@link Direction} to use, defaults to {@link Direction#ASC} if <code>null</code>
         * @return A new {@link Order} with the given {@link Direction}
         */
        public Order with(Direction direction) {
            return new Order(direction, path, ignoreCase, nullHandling);
        }

        /**
         * Returns a new {@link Order} with the same configuration as this {@link Order}, but the given path.
         *
         * @param path The path
         * @return A new {@link Order} with the same configuration as this {@link Order}, but the given path
         */
        public Order withPath(String path) {
            return new Order(direction, path, ignoreCase, nullHandling);
        }

        /**
         * Returns a new {@link Order} with case insensitive sorting enabled.
         *
         * @return A new {@link Order} with case insensitive sorting enabled
         */
        public Order ignoreCase() {
            return new Order(direction, path, true, nullHandling);
        }

        /**
         * Returns a new {@link Order} with the given {@link NullHandling}.
         *
         * @param nullHandling The {@link NullHandling} to use, defaults to {@link NullHandling#NATIVE} if <code>null</code>
         * @return A new {@link Order} with the given {@link NullHandling}
         */
        public Order with(NullHandling nullHandling) {
            return new Order(direction, path, ignoreCase, nullHandling);
        }

        /**
         * Returns a new {@link Order} with {@link NullHandling#NULLS_FIRST} as null handling.
         *
         * @return A new {@link Order} with {@link NullHandling#NULLS_FIRST} as null handling
         */
        public Order nullsFirst() {
            return with(NullHandling.NULLS_FIRST);
        }

        /**
         * Returns a new {@link Order} with {@link NullHandling#NULLS_LAST} as null handling.
         *
         * @return A new {@link Order} with {@link NullHandling#NULLS_LAST} as null handling
         */
        public Order nullsLast() {
            return with(NullHandling.NULLS_LAST);
        }

        /**
         * Returns a new {@link Order} with {@link NullHandling#NATIVE} as null handling.
         *
         * @return A new {@link Order} with {@link NullHandling#NATIVE} as null handling
         */
        public Order nullsNative() {
            return with(NullHandling.NATIVE);
        }

        /**
         * Returns the {@link NullHandling}.
         *
         * @return The {@link NullHandling}
         */
        public NullHandling getNullHandling() {
            return nullHandling;
        }

        /**
         * Appends the string representation to the given {@link StringBuilder}.
         *
         * @param sb The {@link StringBuilder} to append to
         */
        public void appendTo(StringBuilder sb) {
            sb.append(path).append(": ").append(direction);

            if (!NullHandling.NATIVE.equals(nullHandling)) {
                sb.append(", ").append(nullHandling);
            }

            if (ignoreCase) {
                sb.append(", ignoring case");
            }
        }

        @Override
        public int hashCode() {
            int result = direction.hashCode();
            result = 31 * result + path.hashCode();
            result = 31 * result + (ignoreCase ? 1 : 0);
            result = 31 * result + nullHandling.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Order)) {
                return false;
            }

            Order that = (Order) obj;

            return direction == that.direction
                    && path.equals(that.path)
                    && ignoreCase == that.ignoreCase
                    && nullHandling == that.nullHandling;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }
    }
}
