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
