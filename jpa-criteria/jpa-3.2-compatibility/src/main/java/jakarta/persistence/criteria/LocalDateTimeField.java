/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2

package jakarta.persistence.criteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Each instance represents a type of field which can be
 * extracted from a {@link LocalDateTime}.
 *
 * @param <N> the resulting type of the extracted value
 *
 * @since 3.2
 */
public class LocalDateTimeField<N> implements TemporalField<N, LocalDateTime> {

	private final String name;

	private LocalDateTimeField(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * The calendar year.
	 */
	public static final LocalDateTimeField<Integer> YEAR = new LocalDateTimeField<>("year");
	/**
	 * The calendar quarter, numbered from 1 to 4.
	 */
	public static final LocalDateTimeField<Integer> QUARTER = new LocalDateTimeField<>("quarter");
	/**
	 * The calendar month of the year, numbered from 1.
	 */
	public static final LocalDateTimeField<Integer> MONTH = new LocalDateTimeField<>("month");
	/**
	 * The ISO-8601 week number.
	 */
	public static final LocalDateTimeField<Integer> WEEK = new LocalDateTimeField<>("week");
	/**
	 * The calendar day of the month, numbered from 1.
	 */
	public static final LocalDateTimeField<Integer> DAY = new LocalDateTimeField<>("day");

	/**
	 * The hour of the day in 24-hour time, numbered from 0 to 23.
	 */
	public static final LocalDateTimeField<Integer> HOUR = new LocalDateTimeField<>("hour");
	/**
	 * The minute of the hour, numbered from 0 to 59.
	 */
	public static final LocalDateTimeField<Integer> MINUTE = new LocalDateTimeField<>("minute");
	/**
	 * The second of the minute, numbered from 0 to 59, including a fractional
	 * part representing fractions of a second
	 */
	public static final LocalDateTimeField<Double> SECOND = new LocalDateTimeField<>("second");

	/**
	 * The {@linkplain LocalDate date} part of a datetime.
	 */
	public static final LocalDateTimeField<LocalDate> DATE = new LocalDateTimeField<>("date");
	/**
	 * The {@linkplain LocalTime time} part of a datetime.
	 */
	public static final LocalDateTimeField<LocalTime> TIME = new LocalDateTimeField<>("time");
}
