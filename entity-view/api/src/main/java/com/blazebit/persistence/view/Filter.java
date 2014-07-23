package com.blazebit.persistence.view;

import com.blazebit.persistence.Filterable;


/**
 * TODO: javadoc
 * 
 * Filters must have either an empty constructor or a constructor that accepts {@linkplain String} or {@linkplain Object}.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface Filter {

    /**
     * TODO: javadoc
     *
     * @return
     */
    public <T extends Filterable<T>> T apply(T filterable, String expression);
}
