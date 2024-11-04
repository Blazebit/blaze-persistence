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
public class FloatBasicUserType extends ImmutableBasicUserType<Float> {

    public static final BasicUserType<Float> INSTANCE = new FloatBasicUserType();

    @Override
    public Float fromString(CharSequence sequence) {
        return Float.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
