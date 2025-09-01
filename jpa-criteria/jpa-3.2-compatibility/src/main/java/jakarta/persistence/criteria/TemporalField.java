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

import java.time.temporal.Temporal;

/**
 * Each instance represents a type of field which can be
 * extracted from a date, time, or datetime.
 *
 * @param <N> the resulting type of the extracted value
 * @param <T> the temporal type (date, time, or datetime)
 *
 * @see LocalDateField
 * @see LocalTimeField
 * @see LocalDateTimeField
 * @see CriteriaBuilder#extract(TemporalField, Expression)
 *
 * @since 3.2
 */
public interface TemporalField<N,T extends Temporal> {}
