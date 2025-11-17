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
 * Enumerated the options for mapping entity inheritance.
 *
 * @see Inheritance#strategy
 *
 * @since 1.0
 */
public enum InheritanceType { 

    /**
     * A single table for each entity class hierarchy.
     *
     * <p>Subclasses are stored together with the root
     * class, and each entity instance is stored as a
     * single table row.
     *
     * <p>The concrete subclass represented by a row
     * is determined by the value held by a
     * {@linkplain DiscriminatorColumn discriminator
     * column}.
     */
    SINGLE_TABLE, 

    /**
     * A table for each concrete entity class.
     *
     * <p>Each concrete class in the hierarchy has its
     * own table, and each entity instance is stored as
     * a single table row.
     *
     * <p>No discriminator column is necessary.
     */
    TABLE_PER_CLASS, 

    /**
     * A table for each abstract or concrete entity class,
     * with only the columns mapped to persistent fields
     * and properties <em>declared</em> by the entity class.
     *
     * <p>Each class in the hierarchy has its own table,
     * but that table does not contain columns mapped to
     * inherited fields or properties, and so the state of
     * an entity instance might be stored across multiple
     * table rows. A join is used to retrieve the state of
     * such entities.
     */
    JOINED 
}
