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
public class IntegerBasicUserType extends ImmutableBasicUserType<Integer> implements VersionBasicUserType<Integer> {

    public static final BasicUserType<Integer> INSTANCE = new IntegerBasicUserType();
    private static final Integer ZERO = 0;

    @Override
    public Integer nextValue(Integer current) {
        if (current == null) {
            return ZERO;
        }
        return current + 1;
    }

    @Override
    public Integer fromString(CharSequence sequence) {
        return Integer.parseInt(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
