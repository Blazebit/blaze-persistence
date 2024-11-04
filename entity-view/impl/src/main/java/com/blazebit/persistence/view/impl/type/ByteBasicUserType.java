/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ByteBasicUserType extends ImmutableBasicUserType<Byte> {

    public static final BasicUserType<Byte> INSTANCE = new ByteBasicUserType();

    @Override
    public Byte fromString(CharSequence sequence) {
        return Byte.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
