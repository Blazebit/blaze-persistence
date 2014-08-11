package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.filter.LessThanFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class LessThanFilterImpl extends AbstractComparisonFilter implements LessThanFilter {

    public LessThanFilterImpl(Class<?> expectedType, Object value) {
        super(expectedType, value);
    }

    @Override
    protected <T> T applyRestriction(RestrictionBuilder<T> rb) {
        return rb.lt(value);
    }

    @Override
    protected <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb) {
        return rb.lt();
    }

}
