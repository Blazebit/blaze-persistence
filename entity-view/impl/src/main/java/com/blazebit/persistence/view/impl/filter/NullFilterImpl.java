package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.filter.NullFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class NullFilterImpl implements NullFilter {

    private final boolean value;

    public NullFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        if (value instanceof Boolean) {
            this.value = (Boolean) value;
        } else if (value instanceof SubqueryProvider) {
            throw new IllegalArgumentException("Subqueries are not allowed for the NullFilter");
        } else {
            this.value = Boolean.parseBoolean(value.toString());
        }
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        if (value) {
            return rb.isNull();
        } else {
            return rb.isNotNull();
        }
    }
}
