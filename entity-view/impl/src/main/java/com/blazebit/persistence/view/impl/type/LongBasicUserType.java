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
public class LongBasicUserType extends ImmutableBasicUserType<Long> implements VersionBasicUserType<Long> {

    public static final BasicUserType<Long> INSTANCE = new LongBasicUserType();
    private static final Long ZERO = 0L;

    @Override
    public Long nextValue(Long current) {
        if (current == null) {
            return ZERO;
        }
        return current + 1L;
    }

    @Override
    public Long fromString(CharSequence sequence) {
        return Long.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
