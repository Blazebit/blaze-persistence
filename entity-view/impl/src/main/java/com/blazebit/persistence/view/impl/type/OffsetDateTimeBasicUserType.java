/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OffsetDateTimeBasicUserType extends TimestampishImmutableBasicUserType<OffsetDateTime> {

    public static final BasicUserType<OffsetDateTime> INSTANCE = new OffsetDateTimeBasicUserType();

    @Override
    public OffsetDateTime fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalDateTime.parse( input).atOffset( ZoneOffset.UTC );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date-time format: " + input, e);
        }
    }

}
