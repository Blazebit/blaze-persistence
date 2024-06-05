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
