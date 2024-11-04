/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * The default basic user type implementation for immutable types.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ImmutableBasicUserType<X> implements BasicUserType<X> {

    public static final BasicUserType<?> INSTANCE = new ImmutableBasicUserType<>();

    @Override
    public boolean isMutable() {
        return false;
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
    public boolean supportsDeepEqualChecking() {
        return true;
    }

    @Override
    public boolean supportsDeepCloning() {
        return true;
    }

    @Override
    public boolean isEqual(X initial, X current) {
        return initial.equals(current);
    }

    @Override
    public boolean isDeepEqual(X initial, X current) {
        return initial.equals(current);
    }

    @Override
    public int hashCode(X object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(X entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(X entity) {
        return null;
    }

    @Override
    public X deepClone(X object) {
        return object;
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
