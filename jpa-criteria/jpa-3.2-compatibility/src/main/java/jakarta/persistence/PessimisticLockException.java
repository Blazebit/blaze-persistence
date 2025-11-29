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
 * Thrown by the persistence provider when a pessimistic locking conflict
 * occurs. This exception may be thrown as part of an API call, a flush or
 * at commit time. The current transaction, if one is active, is marked
 * for rollback.
 *
 * @since 2.0
 */
public class PessimisticLockException extends PersistenceException {
    /**
     * The object that caused the exception
     */
    Object entity;

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with {@code null} as its detail message.
     */
    public PessimisticLockException() {
        super();
    }

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with the specified detail message.
     * @param   message   the detail message.
     */
    public PessimisticLockException(String message) {
        super(message);
    }

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with the specified detail message and cause.
     * @param   message   the detail message.
     * @param   cause     the cause.
     */
    public PessimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with the specified cause.
     * @param   cause     the cause.
     */
    public PessimisticLockException(Throwable cause) {
        super(cause);
    }

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with the specified entity.
     * @param   entity     the entity.
     */
    public PessimisticLockException(Object entity) {
        this.entity = entity;
    }

    /** 
     * Constructs a new {@code PessimisticLockException} exception 
     * with the specified detail message, cause, and entity.
     * @param   message   the detail message.
     * @param   cause     the cause.
     * @param   entity     the entity.
     */
    public PessimisticLockException(String message, Throwable cause, Object entity) {
        super(message, cause);
        this.entity = entity;
    }
    
    /**
     * Returns the entity that caused this exception.
     * @return the entity.
     */
    public Object getEntity() {
        return this.entity;
    }
}



