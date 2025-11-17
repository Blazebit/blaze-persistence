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
 * Thrown by the persistence provider when {@link EntityManager#persist(Object)}
 * is called and the entity already exists. The current transaction, if one is
 * active, will be marked for rollback.
 *
 * <p>If the entity already exists, the {@code EntityExistsException} may be
 * thrown when the persist operation is invoked, or the {@code EntityExistsException}
 * or another {@link PersistenceException} may be thrown at flush or commit time.
 *
 * <p>The current transaction, if one is active and the persistence context
 * has been joined to it, will be marked for rollback.
 *
 * @see EntityManager#persist(Object)
 * 
 * @since 1.0
 */
public class EntityExistsException extends PersistenceException {

    /**
     * Constructs a new {@code EntityExistsException} exception with
     * {@code null} as its detail message.
     */
    public EntityExistsException() {
        super();
    }

    /**
     * Constructs a new {@code EntityExistsException} exception with the
     * specified detail message.
     * 
     * @param message the detail message.
     */
    public EntityExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code EntityExistsException} exception with the
     * specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public EntityExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code EntityExistsException} exception with the
     * specified cause.
     * 
     * @param cause the cause.
     */
    public EntityExistsException(Throwable cause) {
        super(cause);
    }
}
