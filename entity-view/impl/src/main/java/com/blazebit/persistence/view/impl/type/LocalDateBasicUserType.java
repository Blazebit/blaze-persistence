/*
 * Copyright 2014 - 2024 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class LocalDateBasicUserType extends TimestampishImmutableBasicUserType<LocalDate> {

    public static final BasicUserType<LocalDate> INSTANCE = new LocalDateBasicUserType();
    private static final DateTimeFormatter OPTIONAL_TIME_OFFSET_AND_ZONE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart()
            .optionalStart()
            .appendLiteral(' ')
            .optionalEnd()
            .optionalStart()
            .appendLiteral('T')
            .optionalEnd()
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart()
            .appendOffsetId()
            .optionalEnd()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneRegionId()
            .appendLiteral(']')
            .optionalEnd()
            .optionalEnd().toFormatter();

    @Override
    public LocalDate fromString(CharSequence sequence) {
        String input = sequence.toString();
        try {
            return LocalDate.parse(input, OPTIONAL_TIME_OFFSET_AND_ZONE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date or date-time format: " + input, e);
        }
    }
}
