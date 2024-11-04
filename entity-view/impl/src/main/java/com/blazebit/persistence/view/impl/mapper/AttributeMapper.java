/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributeMapper<S, T> implements Mapper<S, T> {

    private final AttributeAccessor[] sourceAttributes;
    private final AttributeAccessor[] targetAttributes;

    AttributeMapper(List<AttributeAccessor> source, List<AttributeAccessor> target) {
        if (source.size() != target.size()) {
            throw new IllegalArgumentException("Invalid size");
        }
        this.sourceAttributes = source.toArray(new AttributeAccessor[source.size()]);
        this.targetAttributes = target.toArray(new AttributeAccessor[target.size()]);
    }

    @Override
    public void map(S source, T target) {
        for (int i = 0; i < sourceAttributes.length; i++) {
            Object sourceValue = sourceAttributes[i].getValue(source);
            targetAttributes[i].setValue(target, sourceValue);
        }
    }
    
}
