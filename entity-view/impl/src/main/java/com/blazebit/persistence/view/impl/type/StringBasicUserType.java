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
public class StringBasicUserType extends ImmutableBasicUserType<String> {

    public static final BasicUserType<String> INSTANCE = new StringBasicUserType();

    @Override
    public String fromString(CharSequence sequence) {
        return sequence.toString();
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
