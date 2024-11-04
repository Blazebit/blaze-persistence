/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class OffsetTimeBasicUserType extends ImmutableBasicUserType<OffsetTime> {

    public static final BasicUserType<OffsetTime> INSTANCE = new OffsetTimeBasicUserType();

    @Override
    public OffsetTime fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalTime.parse(input).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + input, e);
        }
    }

    @Override
    public String toStringExpression(String expression) {
        return "TIME_ISO(" + expression + ")";
    }

}
