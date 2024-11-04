/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * A base class for implementing basic user types for non-entity mutable types.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMutableBasicUserType<X> implements BasicUserType<X> {

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        return true;
    }

    @Override
    public boolean supportsDeepCloning() {
        return true;
    }

    @Override
    public boolean isEqual(X initial, X current) {
        return isDeepEqual(initial, current);
    }

    @Override
    public boolean isDeepEqual(X object1, X object2) {
        return object1.equals(object2);
    }

    @Override
    public int hashCode(X object) {
        return object.hashCode();
    }

    @Override
    public boolean supportsDirtyChecking() {
        return false;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
    }

    @Override
    public boolean shouldPersist(X entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(X entity) {
        return DIRTY_MARKER;
    }

    @Override
    public X fromString(CharSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toStringExpression(String expression) {
        throw new UnsupportedOperationException();
    }
}
