package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.view.Filter;


/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ContainsIgnoreCaseFilter implements Filter {

    private final String value;

    /**
     * Constructs a new {@linkplain ContainsFilter} which only retains elements that contain the given value in the left hand side.
     * 
     * @param value The value the left hand side has to contain
     */
    public ContainsIgnoreCaseFilter(String value) {
        this.value = value;
    }

    @Override
    public <T extends Filterable<T>> T apply(T filterable, String expression) {
        return filterable.where(expression).like("%" + value + "%", false);
    }
}
