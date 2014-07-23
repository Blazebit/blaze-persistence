package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.view.Filter;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ExactFilter implements Filter {
    
    private final Object value;

    /**
     * Constructs a new {@linkplain ExactFilter} which only retains elements where the given value and the left hand side are equal.
     * 
     * @param value The value the left hand side has to equal to
     */
    public ExactFilter(Object value) {
        this.value = value;
    }

    @Override
    public <T extends Filterable<T>> T apply(T filterable, String expression) {
        return filterable.where(expression).eq(value);
    }

}
