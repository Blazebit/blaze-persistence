/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CollectionAddMapper<S, T> implements Mapper<S, T> {

    private final AttributeAccessor attributeAccessor;

    public CollectionAddMapper(AttributeAccessor attributeAccessor) {
        this.attributeAccessor = attributeAccessor;
    }

    @Override
    public void map(S source, T target) {
        Collection<Object> value = (Collection<Object>) attributeAccessor.getValue(target);
        if (value != null) {
            value.add(source);
        }
    }
    
}
