/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class LocalDateTimeBasicUserType extends TimestampishImmutableBasicUserType<LocalDateTime> {

    public static final BasicUserType<LocalDateTime> INSTANCE = new LocalDateTimeBasicUserType();

    @Override
    public LocalDateTime fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date-time format: " + input, e);
        }
    }

}
