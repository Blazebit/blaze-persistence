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
 * Enumerates the defined primary key generation strategies.
 *
 * @see GeneratedValue
 *
 * @since 1.0
 */
public enum GenerationType { 

    /**
     * Indicates that the persistence provider must assign 
     * primary keys for the entity using an underlying 
     * database table to ensure uniqueness.
     *
     * <p>May be used to generate primary keys of type
     * {@link Long}, {@link Integer}, {@code long}, or
     * {@code int}.
     */
    TABLE, 

    /**
     * Indicates that the persistence provider must assign 
     * primary keys for the entity using a database sequence.
     *
     * <p>May be used to generate primary keys of type
     * {@link Long}, {@link Integer}, {@code long}, or
     * {@code int}.
     */
    SEQUENCE, 

    /**
     * Indicates that the persistence provider must assign 
     * primary keys for the entity using a database identity
     * column.
     *
     * <p>May be used to generate primary keys of type
     * {@link Long}, {@link Integer}, {@code long}, or
     * {@code int}.
     */
    IDENTITY,

    /**
     * Indicates that the persistence provider must assign
     * primary keys for the entity by generating an RFC 4122
     * Universally Unique IDentifier.
     *
     * <p>May be used to generate primary keys of type
     * {@link java.util.UUID} or {@link String}.
     */
    UUID,

    /**
     * Indicates that the persistence provider should pick an 
     * appropriate strategy for the particular database.
     * <ul>
     * <li>For a primary key of type {@link java.util.UUID} or
     *    {@link String}, this is equivalent to {@link #UUID}.
     * <li>For a primary key of type {@link Long}, {@link Integer},
     *    {@code long}, or {@code int}, the provider selects
     *    between {@link #TABLE}, {@link #SEQUENCE}, and
     *    {@link #IDENTITY}.
     * </ul>
     *
     * <p>The {@code AUTO} generation strategy may expect a
     * database resource to exist, or it may attempt to create
     * one. A vendor may provide documentation on how to create
     * such resources in the event that it does not support
     * schema generation or cannot create the schema resource
     * at runtime.
     */
    AUTO
}
