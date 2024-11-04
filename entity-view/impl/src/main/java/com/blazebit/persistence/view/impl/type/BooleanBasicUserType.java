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
public class BooleanBasicUserType extends ImmutableBasicUserType<Boolean> {

    public static final BasicUserType<Boolean> INSTANCE = new BooleanBasicUserType();

    @Override
    public Boolean fromString(CharSequence sequence) {
        return sequence.charAt(0) == 't';
    }

    @Override
    public String toStringExpression(String expression) {
        return "CASE WHEN " + expression + " = TRUE THEN 'true' ELSE 'false' END";
    }
}
