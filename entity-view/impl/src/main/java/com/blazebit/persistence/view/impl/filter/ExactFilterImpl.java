package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.filter.ExactFilter;

public class ExactFilterImpl extends AbstractComparisonFilter implements ExactFilter {

    public ExactFilterImpl(Class<?> expectedType, Object value) {
        super(expectedType, value);
    }

    @Override
    protected <T> T applyRestriction(RestrictionBuilder<T> rb) {
        return rb.eq(value);
    }

    @Override
    protected <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb) {
        return rb.eq();
    }

}
