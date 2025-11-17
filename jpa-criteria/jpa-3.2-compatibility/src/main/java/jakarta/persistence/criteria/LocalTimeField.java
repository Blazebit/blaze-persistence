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

import java.time.LocalTime;

/**
 * Each instance represents a type of field which can be
 * extracted from a {@link LocalTime}.
 *
 * @param <N> the resulting type of the extracted value
 *
 * @since 3.2
 */
public class LocalTimeField<N> implements TemporalField<N, LocalTime> {

	private final String name;

	private LocalTimeField(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * The hour of the day in 24-hour time, numbered from 0 to 23.
	 */
	public static final LocalTimeField<Integer> HOUR = new LocalTimeField<>("hour");
	/**
	 * The minute of the hour, numbered from 0 to 59.
	 */
	public static final LocalTimeField<Integer> MINUTE = new LocalTimeField<>("minute");
	/**
	 * The second of the minute, numbered from 0 to 59, including a fractional
	 * part representing fractions of a second
	 */
	public static final LocalTimeField<Double> SECOND = new LocalTimeField<>("second");
}
