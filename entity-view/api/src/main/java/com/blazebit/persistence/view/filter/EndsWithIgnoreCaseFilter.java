package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.view.Filter;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EndsWithIgnoreCaseFilter implements Filter {

    private final String value;

    /**
     * Constructs a new {@linkplain EndsWithFilter} which only retains elements that end the given value in the left hand side.
     * 
     * @param value The value the left hand side has to end with
     */
    public EndsWithIgnoreCaseFilter(String value) {
        this.value = value;
    }

    @Override
    public <T extends Filterable<T>> T apply(T filterable, String expression) {
        return filterable.where(expression).like("%" + value, false);
    }
}
