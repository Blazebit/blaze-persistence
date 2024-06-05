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

package com.blazebit.persistence.view.testsuite.fetch.multiset.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.timeentity.DocumentForMultisetFetch;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@EntityView(DocumentForMultisetFetch.class)
public interface DocumentMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    public Instant getTheInstant();

    public LocalDate getTheLocalDate();

    public LocalDateTime getTheLocalDateTime();

    public LocalTime getTheLocalTime();

    public OffsetDateTime getTheOffsetDateTime();

    public OffsetTime getTheOffsetTime();

    public ZonedDateTime getTheZonedDateTime();

    public Date getTheDate();

    public Time getTheTime();

    public Timestamp getTheTimestamp();

}
