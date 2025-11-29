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
 * Thrown by the persistence provider when an entity reference obtained by
 * {@link EntityManager#getReference EntityManager.getReference}
 * is accessed but the entity does not exist. Thrown when
 * {@link EntityManager#refresh EntityManager.refresh} is called and the
 * object no longer exists in the database. 
 * Thrown when {@link EntityManager#lock EntityManager.lock} is used with
 * pessimistic locking is used and the entity no longer exists in the database.
 * <p> The current transaction, if one is active and the persistence context
 * has been joined to it, will be marked for rollback.
 * 
 * @see EntityManager#getReference(Class,Object)
 * @see EntityManager#refresh(Object)
 * @see EntityManager#refresh(Object, LockModeType)
 * @see EntityManager#refresh(Object, java.util.Map)
 * @see EntityManager#refresh(Object, LockModeType, java.util.Map)
 * @see EntityManager#lock(Object, LockModeType)
 * @see EntityManager#lock(Object, LockModeType, java.util.Map)
 * 
 * @since 1.0
 */
public class EntityNotFoundException extends PersistenceException {

	/**
	 * Constructs a new {@code EntityNotFoundException} exception with
	 * {@code null} as its detail message.
	 */
	public EntityNotFoundException() {
		super();
	}

	/**
	 * Constructs a new {@code EntityNotFoundException} exception with
	 * {@code null} as its detail message.
	 */
	public EntityNotFoundException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@code EntityNotFoundException} exception with the
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code EntityNotFoundException} exception with the
	 * specified detail message.
	 *
	 * @param message the detail message.
	 */
	public EntityNotFoundException(String message, Exception cause) {
		super(message, cause);
	}

}
