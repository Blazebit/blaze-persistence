/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.filter.ContainsFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ContainsFilterImpl<FilterValue> extends ContainsFilter<FilterValue> {

    private final String value;

    public ContainsFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        this.value = "%" + value + "%";
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        return rb.like().value(value).noEscape();
    }
}
