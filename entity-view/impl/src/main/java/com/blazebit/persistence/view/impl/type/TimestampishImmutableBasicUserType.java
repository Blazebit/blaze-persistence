/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 * The default basic user type implementation for timestampish immutable types.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class TimestampishImmutableBasicUserType<X> extends ImmutableBasicUserType<X> {

    @Override
    public abstract X fromString(CharSequence sequence);

    @Override
    public String toStringExpression(String expression) {
        return "TIMESTAMP_ISO(" + expression + ")";
    }
}
