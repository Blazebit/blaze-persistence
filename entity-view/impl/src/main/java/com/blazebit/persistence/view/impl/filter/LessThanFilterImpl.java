/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.filter.LessThanFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class LessThanFilterImpl<FilterValue> extends LessThanFilter<FilterValue> implements ComparisonFilter {

    private final ComparisonFilterHelper helper;

    public LessThanFilterImpl(Class<?> expectedType, Object value) {
        this.helper = new ComparisonFilterHelper(this, expectedType, value);
    }

    @Override
    protected <T> T apply(RestrictionBuilder<T> restrictionBuilder) {
        return helper.apply(restrictionBuilder);
    }

    @Override
    public <T> T applyRestriction(RestrictionBuilder<T> rb, Object value) {
        return rb.lt(value);
    }

    @Override
    public <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb) {
        return rb.lt();
    }

}
