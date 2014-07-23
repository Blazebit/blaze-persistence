package com.blazebit.persistence.view;

import com.blazebit.persistence.Sortable;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class Sorters {
    
    private Sorters() {
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Sorter sorter(boolean ascending, boolean nullFirst) {
        return new DefaultSorter(ascending, nullFirst);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Sorter ascending(boolean nullFirst) {
        return new DefaultSorter(true, nullFirst);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Sorter descending(boolean nullFirst) {
        return new DefaultSorter(false, nullFirst);
    }

    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Sorter ascending() {
        return new DefaultSorter(true, false);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
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
