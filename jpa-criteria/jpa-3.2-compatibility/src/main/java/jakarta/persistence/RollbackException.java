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
 * Thrown by the persistence provider when
 * {@link EntityTransaction#commit()} fails.
 *
 * @see EntityTransaction#commit()
 *
 * @since 1.0
 */
public class RollbackException extends PersistenceException {
	
        /** 
         * Constructs a new {@code RollbackException} exception 
         * with {@code null} as its detail message.
         */
	public RollbackException() {
		super();
	}

        /** 
         * Constructs a new {@code RollbackException} exception 
         * with the specified detail message.
         * @param   message   the detail message.
         */
	public RollbackException(String message) {
		super(message);
	}

        /** 
         * Constructs a new {@code RollbackException} exception 
         * with the specified detail message and cause.
         * @param   message   the detail message.
         * @param   cause     the cause.
         */
	public RollbackException(String message, Throwable cause) {
		super(message, cause);
	}
	
        /** 
         * Constructs a new {@code RollbackException} exception 
         * with the specified cause.
         * @param   cause     the cause.
         */
	public RollbackException(Throwable cause) {
		super(cause);
	}
}


