/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class PassthroughAttributeAccessor implements AttributeAccessor {

    public static final AttributeAccessor INSTANCE = new PassthroughAttributeAccessor();

    private PassthroughAttributeAccessor() {
    }

    @Override
    public void setValue(Object object, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOrCreateValue(Object object) {
        return object;
    }

    @Override
    public Object getValue(Object object) {
        return object;
    }
}
