/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class InstantBasicUserType extends TimestampishImmutableBasicUserType<Instant> {

    public static final BasicUserType<Instant> INSTANCE = new InstantBasicUserType();

    @Override
    public Instant fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalDateTime.parse( input).toInstant( ZoneOffset.UTC );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date-time format: " + input, e);
        }
    }

}
