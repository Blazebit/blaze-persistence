/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NestedAttributeAccessor implements AttributeAccessor {

    private final AttributeAccessor[] accessors;

    NestedAttributeAccessor(List<AttributeAccessor> accessors) {
        this.accessors = accessors.toArray(new AttributeAccessor[accessors.size()]);
    }

    @Override
    public void setValue(Object entity, Object value) {
        if (entity == null || accessors.length == 0) {
            return;
        }

        for (int i = 0; i < accessors.length - 1; i++) {
            entity = accessors[i].getOrCreateValue(entity);
        }

        accessors[accessors.length - 1].setValue(entity, value);
    }

    @Override
    public Object getOrCreateValue(Object entity) {
        if (entity == null || accessors.length == 0) {
            return entity;
        }

        Object value = entity;
        for (int i = 0; i < accessors.length - 1; i++) {
            value = accessors[i].getOrCreateValue(value);
        }

        return accessors[accessors.length - 1].getValue(value);
    }

    @Override
    public Object getValue(Object entity) {
        if (entity == null || accessors.length == 0) {
            return entity;
        }

        Object value = entity;
        for (int i = 0; i < accessors.length - 1; i++) {
            value = accessors[i].getValue(value);
            if (value == null) {
                return null;
            }
        }

        return accessors[accessors.length - 1].getValue(value);
    }
}
