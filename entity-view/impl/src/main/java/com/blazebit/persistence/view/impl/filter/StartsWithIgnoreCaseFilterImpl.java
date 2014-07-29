package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.filter.StartsWithIgnoreCaseFilter;


public class StartsWithIgnoreCaseFilterImpl implements StartsWithIgnoreCaseFilter {

    private final String value;

    public StartsWithIgnoreCaseFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        this.value = value + "%";
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        return rb.like(value, false);
    }
}
