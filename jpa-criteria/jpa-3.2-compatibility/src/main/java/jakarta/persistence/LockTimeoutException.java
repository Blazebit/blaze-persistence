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
 * Thrown by the persistence provider when a pessimistic locking
 * conflict occurs that does not result in transaction rollback.
 * This exception may be thrown as part of an API call, at, flush
 * or at commit time. The current transaction, if one is active,
 * will be not be marked for rollback.
 *
 * @since 2.0
 */
public class LockTimeoutException extends PersistenceException {
    /** The object that caused the exception */
    Object entity;

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with {@code null} as its detail message.
     */
    public LockTimeoutException() {
        super();
    }

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with the specified detail message.
     * @param   message   the detail message.
     */
    public LockTimeoutException(String message) {
        super(message);
    }

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with the specified detail message and cause.
     * @param   message   the detail message.
     * @param   cause     the cause.
     */
    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with the specified cause.
     * @param   cause     the cause.
     */
    public LockTimeoutException(Throwable cause) {
        super(cause);
    }

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with the specified object.
     * @param   entity     the entity.
     */
    public LockTimeoutException(Object entity) {
        this.entity = entity;
    }

    /** 
     * Constructs a new {@code LockTimeoutException} exception
     * with the specified detail message, cause, and entity.
     * @param   message   the detail message.
     * @param   cause     the cause.
     * @param   entity     the entity.
     */
    public LockTimeoutException(String message, Throwable cause, Object entity) {
        super(message, cause);
        this.entity = entity;
    }
    
    /**
     * Returns the object that caused this exception.
     * @return the entity
     */
    public Object getObject() {
        return this.entity;
    }
}


