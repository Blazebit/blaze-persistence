/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ShortBasicUserType extends ImmutableBasicUserType<Short> implements VersionBasicUserType<Short> {

    public static final BasicUserType<Short> INSTANCE = new ShortBasicUserType();
    private static final Short ZERO = 0;

    @Override
    public Short nextValue(Short current) {
        if (current == null) {
            return ZERO;
        }
        return (short) (current + ((short) 1));
    }

    @Override
    public Short fromString(CharSequence sequence) {
        return Short.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
