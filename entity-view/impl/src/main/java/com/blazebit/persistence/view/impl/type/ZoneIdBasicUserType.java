/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.ZoneId;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ZoneIdBasicUserType extends ImmutableBasicUserType<ZoneId> {

    public static final BasicUserType<ZoneId> INSTANCE = new ZoneIdBasicUserType();

    @Override
    public ZoneId fromString(CharSequence sequence) {
        return ZoneId.of(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }

}
