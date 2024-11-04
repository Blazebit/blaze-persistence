/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class LocalDateBasicUserType extends ImmutableBasicUserType<LocalDate> {

    public static final BasicUserType<LocalDate> INSTANCE = new LocalDateBasicUserType();

    @Override
    public LocalDate fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + input, e);
        }
    }

    @Override
    public String toStringExpression(String expression) {
        return "DATE_ISO(" + expression + ")";
    }
}
