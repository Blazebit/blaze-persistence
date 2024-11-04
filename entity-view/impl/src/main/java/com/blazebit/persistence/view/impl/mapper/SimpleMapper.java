/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleMapper<S, T> implements Mapper<S, T> {

    private final AttributeAccessor attributeAccessor;

    public SimpleMapper(AttributeAccessor attributeAccessor) {
        this.attributeAccessor = attributeAccessor;
    }

    @Override
    public void map(S source, T target) {
        attributeAccessor.setValue(target, source);
    }

    public AttributeAccessor getAttributeAccessor() {
        return attributeAccessor;
    }
}
