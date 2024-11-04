/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.util.UUID;


/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class UUIDBasicUserType extends ImmutableBasicUserType<UUID> {

    public static final BasicUserType<UUID> INSTANCE = new UUIDBasicUserType();

    @Override
    public UUID fromString(CharSequence sequence) {
        return UUID.fromString(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
