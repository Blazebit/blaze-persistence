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
 * Thrown by the persistence provider when a query times out
 * and only the statement is rolled back.
 * The current transaction, if one is active, will be not
 * be marked for rollback.
 *
 * @since 2.0
 */
public class QueryTimeoutException extends PersistenceException {

    /**
     * The query object that caused the exception
     */
    Query query;

    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with {@code null} as its detail message.
     */
    public QueryTimeoutException() {
        super();
    }

    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with the specified detail message.
     * @param   message   the detail message.
     */
    public QueryTimeoutException(String message) {
        super(message);
    }

    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with the specified detail message and cause.
     * @param   message   the detail message.
     * @param   cause     the cause.
     */
    public QueryTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with the specified cause.
     * @param   cause     the cause.
     */
    public QueryTimeoutException(Throwable cause) {
        super(cause);
    }


    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with the specified query.
     * @param   query     the query.
     */
    public QueryTimeoutException(Query query) {
        this.query = query;
    }

    /** 
     * Constructs a new {@code QueryTimeoutException} exception
     * with the specified detail message, cause, and query.
     * @param   message   the detail message.
     * @param   cause     the cause.
     * @param   query     the query.
     */
    public QueryTimeoutException(String message, Throwable cause, Query query) {
        super(message, cause);
        this.query = query;
    }
    
    /**
     * Returns the query that caused this exception.
     * @return the query.
     */
    public Query getQuery() {
        return this.query;
    }
}


