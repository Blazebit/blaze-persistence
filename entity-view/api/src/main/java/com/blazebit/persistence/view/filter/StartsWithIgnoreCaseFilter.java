package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.view.Filter;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class StartsWithIgnoreCaseFilter implements Filter {

    private final String value;

    /**
     * Constructs a new {@linkplain StartsWithFilter} which only retains elements that start with the given value in the left hand side.
     * 
     * @param value The value the left hand side has to start with
     */
    public StartsWithIgnoreCaseFilter(String value) {
        this.value = value;
    }

    @Override
    public <T extends Filterable<T>> T apply(T filterable, String expression) {
        return filterable.where(expression).like(value + "%", false);
    }
}
