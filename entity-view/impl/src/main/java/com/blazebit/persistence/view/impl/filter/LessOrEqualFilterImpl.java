package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.filter.LessOrEqualFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class LessOrEqualFilterImpl extends AbstractComparisonFilter implements LessOrEqualFilter {

    public LessOrEqualFilterImpl(Class<?> expectedType, Object value) {
        super(expectedType, value);
    }

    @Override
    protected <T> T applyRestriction(RestrictionBuilder<T> rb) {
        return rb.le(value);
    }

    @Override
    protected <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb) {
        return rb.le();
    }
}
