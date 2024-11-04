/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPluralUserTypeWrapper<C, V> implements BasicUserType<C> {

    protected final BasicUserType<V> elementUserType;

    public AbstractPluralUserTypeWrapper(BasicUserType<V> elementUserType) {
        this.elementUserType = elementUserType;
    }

    @Override
    public boolean isMutable() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDirtyChecking() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean supportsDeepCloning() {
        throw new UnsupportedOperationException("plural attribute");
    }

    public boolean isEqual(C object1, C object2) {
        throw new UnsupportedOperationException("plural attribute");
    }

    public boolean isDeepEqual(C object1, C object2) {
        throw new UnsupportedOperationException("plural attribute");
    }

    public int hashCode(C object) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public boolean shouldPersist(C entity) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public String[] getDirtyProperties(C object) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public C fromString(CharSequence sequence) {
        throw new UnsupportedOperationException("plural attribute");
    }

    @Override
    public String toStringExpression(String expression) {
        throw new UnsupportedOperationException();
    }
}
