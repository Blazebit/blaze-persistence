/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.multisetbig.model;

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
import com.blazebit.persistence.view.testsuite.timeentity.DocumentForMultisetFetch;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@EntityView(DocumentForMultisetFetch.class)
public interface DocumentTemporalsView {

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
