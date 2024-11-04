/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.spi.JpaProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityIdAttributeAccessor implements AttributeAccessor {

    private final JpaProvider jpaProvider;

    public EntityIdAttributeAccessor(JpaProvider jpaProvider) {
        this.jpaProvider = jpaProvider;
    }

    @Override
    public Object getValue(Object entity) {
        return jpaProvider.getIdentifier(entity);
    }

    @Override
    public Object getOrCreateValue(Object entity) {
        throw new UnsupportedOperationException("Read only!");
    }

    @Override
    public void setValue(Object entity, Object value) {
        throw new UnsupportedOperationException("Read only!");
    }
}
