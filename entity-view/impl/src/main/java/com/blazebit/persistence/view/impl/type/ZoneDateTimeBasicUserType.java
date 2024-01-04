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

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ZoneDateTimeBasicUserType extends TimestampishImmutableBasicUserType<ZonedDateTime> {

    public static final BasicUserType<ZonedDateTime> INSTANCE = new ZoneDateTimeBasicUserType();

    @Override
    public ZonedDateTime fromString(CharSequence sequence) {
        return Timestamp.valueOf(sequence.toString()).toInstant().atZone(ZoneOffset.UTC);
    }

}
