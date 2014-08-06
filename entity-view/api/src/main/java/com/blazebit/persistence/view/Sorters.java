package com.blazebit.persistence.view;

import com.blazebit.persistence.Sortable;

/**
 * A utility class that provides methods to create standard sorters.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class Sorters {

    private Sorters() {
    }

    /**
     * Creates and returns a new {@link Sorter} that
     * <ul>
     * <li>sorts ascending if the flag <code>ascending</code> is true, descending otherwise</li>
     * <li>sorts nulls first is the flag <code>nullsFirst</code> is ture, nulls last otherwise</li>
     * </ul>
     *
     * @param ascending If true sorts ascending, otherwise descending
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
     * Like {@link Sorters#ascending(boolean, boolean)} but with <code>nullsFirst</code> set to false.
     * 
     * @return A new sorter
     */
    public static Sorter ascending() {
        return new DefaultSorter(true, false);
    }

    /**
     * Like {@link Sorters#descending(boolean, boolean)} but with <code>nullsFirst</code> set to false.
     * 
     * @return A new sorter
     */
    public static Sorter descending() {
        return new DefaultSorter(false, false);
    }

    private static class DefaultSorter implements Sorter {

        private final boolean ascending;
        private final boolean nullFirst;

        DefaultSorter(boolean ascending, boolean nullFirst) {
            this.ascending = ascending;
            this.nullFirst = nullFirst;
        }

        @Override
        public <T extends Sortable<T>> T apply(T sortable, String expression) {
            return sortable.orderBy(expression, ascending, nullFirst);
        }

    }
}
