package com.blazebit.persistence.view;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;


/**
 * TODO: javadoc
 * 
 * Filters must have either an empty constructor or a constructor that accepts {@linkplain Object}.
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
    public <T> T apply(RestrictionBuilder<T> rb);
}
