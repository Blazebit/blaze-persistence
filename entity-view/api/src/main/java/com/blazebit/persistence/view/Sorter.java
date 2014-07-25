package com.blazebit.persistence.view;

import com.blazebit.persistence.Sortable;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface Sorter {

    /**
     * TODO: javadoc
     *
     * @return
     */
    public <T extends Sortable<T>> T apply(T sortable, String expression);

}
