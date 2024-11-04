/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ComparisonFilterHelper {

    private final ComparisonFilter filter;
    private final Object value;
    private final SubqueryProvider subqueryProvider;

    public ComparisonFilterHelper(ComparisonFilter filter, Class<?> expectedType, Object value) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.filter = filter;

        if (value instanceof SubqueryProvider) {
            this.value = null;
            this.subqueryProvider = (SubqueryProvider) value;
        } else if (expectedType.isInstance(value)) {
            this.value = value;
            this.subqueryProvider = null;
        } else {
            this.value = FilterUtils.parseValue(expectedType, value);
            this.subqueryProvider = null;
        }
    }

    public <T> T apply(RestrictionBuilder<T> rb) {
        if (subqueryProvider == null) {
            return filter.applyRestriction(rb, value);
        } else {
            return subqueryProvider.createSubquery(filter.applySubquery(rb));
        }
    }
}
