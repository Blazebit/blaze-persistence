/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.filter.GreaterOrEqualFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class GreaterOrEqualFilterImpl<FilterValue> extends GreaterOrEqualFilter<FilterValue> implements ComparisonFilter {

    private final ComparisonFilterHelper helper;

    public GreaterOrEqualFilterImpl(Class<?> expectedType, Object value) {
        this.helper = new ComparisonFilterHelper(this, expectedType, value);
    }

    @Override
    protected <T> T apply(RestrictionBuilder<T> restrictionBuilder) {
        return helper.apply(restrictionBuilder);
    }

    @Override
    public <T> T applyRestriction(RestrictionBuilder<T> rb, Object value) {
        return rb.ge(value);
    }

    @Override
    public <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb) {
        return rb.ge();
    }
}
