/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0


package jakarta.persistence;

/**
 * Defines strategies for fetching data from the database.
 * <ul>
 * <li>The {@link #EAGER} strategy is a requirement on the
 *     persistence provider runtime that data must be eagerly
 *     fetched.
 * <li>The {@link #LAZY} strategy is a hint to the persistence
 *     provider runtime that data should be fetched lazily when
 *     it is first accessed. The implementation is permitted to
 *     eagerly fetch data for which the {@code LAZY} strategy
 *     hint has been specified.
 * </ul>
 *
 * <p>Example:
 * {@snippet :
 * @Basic(fetch = LAZY)
 * protected String getName() { return name; }
 * }
 *
 * @see Basic
 * @see ElementCollection
 * @see ManyToMany
 * @see OneToMany
 * @see ManyToOne
 * @see OneToOne
 *
 * @since 1.0
 */
public enum FetchType {

    /**
     * Data may be lazily fetched.
     */
    LAZY,

    /**
     * Data must be eagerly fetched.
     */
    EAGER
}
