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
 * Thrown by the persistence provider when a problem occurs.
 *
 * <p>All instances of {@code PersistenceException}, except for instances
 * of {@link NoResultException}, {@link NonUniqueResultException},
 * {@link LockTimeoutException}, and {@link QueryTimeoutException},
 * cause the current transaction, if one is active and if the persistence
 * context has been joined to it, to be marked for rollback.
 *
 * @since 1.0
 */
public class PersistenceException extends RuntimeException {

    /** 
     * Constructs a new {@code PersistenceException} exception
     * with {@code null} as its detail message.
     */
    public PersistenceException() {
        super();
    }

	/**
	 * Constructs a new {@code PersistenceException} exception
	 * with the specified detail message.
	 * @param   message   the detail message.
	 */
    public PersistenceException(String message) {
        super(message);
    }

	/**
	 * Constructs a new {@code PersistenceException} exception
	 * with the specified detail message and cause.
	 * @param   message   the detail message.
	 * @param   cause     the cause.
	 */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
    
	/**
	 * Constructs a new {@code PersistenceException} exception
	 * with the specified cause.
	 * @param   cause     the cause.
	 */
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}

