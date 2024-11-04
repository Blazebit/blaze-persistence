/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.ZoneOffset;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ZoneOffsetBasicUserType extends ImmutableBasicUserType<ZoneOffset> {

    public static final BasicUserType<ZoneOffset> INSTANCE = new ZoneOffsetBasicUserType();

    @Override
    public String toStringExpression(String expression) {
        throw new UnsupportedOperationException("No timezone offset second extraction implemented yet!");
    }

    @Override
    public ZoneOffset fromString(CharSequence sequence) {
        return ZoneOffset.ofTotalSeconds(Integer.parseInt(sequence.toString()));
    }

}
