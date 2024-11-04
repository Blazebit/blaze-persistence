/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * The default basic user type implementation for unknown types.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MutableBasicUserType<X> extends AbstractMutableBasicUserType<X> {

    public static final BasicUserType<?> INSTANCE = new MutableBasicUserType<>();

    @Override
    public boolean supportsDeepEqualChecking() {
        return false;
    }

    @Override
    public boolean supportsDeepCloning() {
        return false;
    }

    @Override
    public boolean isEqual(X initial, X current) {
        return false;
    }

    @Override
    public boolean isDeepEqual(X initial, X current) {
        return false;
    }

    @Override
    public int hashCode(X object) {
        return 0;
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
