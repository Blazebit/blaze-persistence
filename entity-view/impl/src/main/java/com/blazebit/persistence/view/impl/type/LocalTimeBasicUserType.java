/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class LocalTimeBasicUserType extends ImmutableBasicUserType<LocalTime> {

    public static final BasicUserType<LocalTime> INSTANCE = new LocalTimeBasicUserType();

    @Override
    public LocalTime fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + input, e);
        }
    }

    @Override
    public String toStringExpression(String expression) {
        return "TIME_ISO(" + expression + ")";
    }

}
