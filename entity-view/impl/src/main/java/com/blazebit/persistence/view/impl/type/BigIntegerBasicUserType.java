/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.math.BigInteger;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class BigIntegerBasicUserType extends ImmutableBasicUserType<BigInteger> {

    public static final BasicUserType<BigInteger> INSTANCE = new BigIntegerBasicUserType();

    @Override
    public BigInteger fromString(CharSequence sequence) {
        return new BigInteger(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
