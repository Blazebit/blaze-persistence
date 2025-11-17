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

/**
 * Each instance represents a type of field which can be
 * extracted from a {@link LocalDate}.
 *
 * @param <N> the resulting type of the extracted value
 *
 * @since 3.2
 */
public class LocalDateField<N> implements TemporalField<N, LocalDate> {

	private final String name;

	private LocalDateField(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * The calendar year.
	 */
	public static final LocalDateField<Integer> YEAR = new LocalDateField<>("year");
	/**
	 * The calendar quarter, numbered from 1 to 4.
	 */
	public static final LocalDateField<Integer> QUARTER = new LocalDateField<>("quarter");
	/**
	 * The calendar month of the year, numbered from 1.
	 */
	public static final LocalDateField<Integer> MONTH = new LocalDateField<>("month");
	/**
	 * The ISO-8601 week number.
	 */
	public static final LocalDateField<Integer> WEEK = new LocalDateField<>("week");
	/**
	 * The calendar day of the month, numbered from 1.
	 */
	public static final LocalDateField<Integer> DAY = new LocalDateField<>("day");
}
