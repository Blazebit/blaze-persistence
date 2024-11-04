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
 * Thrown by the persistence provider when a transaction is required but is not
 * active.
 * 
 * @since 1.0
 */
public class TransactionRequiredException extends PersistenceException {

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * {@code null} as its detail message.
	 */
	public TransactionRequiredException() {
		super();
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * {@code null} as its detail message.
	 */
	public TransactionRequiredException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * the specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public TransactionRequiredException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public TransactionRequiredException(String message, Exception cause) {
		super(message, cause);
	}
}
