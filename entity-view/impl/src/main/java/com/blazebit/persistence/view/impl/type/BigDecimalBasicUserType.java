/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.math.BigDecimal;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class BigDecimalBasicUserType extends ImmutableBasicUserType<BigDecimal> {

    public static final BasicUserType<BigDecimal> INSTANCE = new BigDecimalBasicUserType();

    @Override
    public BigDecimal fromString(CharSequence sequence) {
        return new BigDecimal(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
