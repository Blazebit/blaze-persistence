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
public class DoubleBasicUserType extends ImmutableBasicUserType<Double> {

    public static final BasicUserType<Double> INSTANCE = new DoubleBasicUserType();

    @Override
    public Double fromString(CharSequence sequence) {
        return Double.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
