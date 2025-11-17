/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Gavin King      - 3.2


package jakarta.persistence;

/**
 * A function which makes use of a native database connection to compute
 * a result. The connection is usually a JDBC connection.
 *
 * @param <C> the connection type, usually {@code java.sql.Connection}
 *
 * @see ConnectionConsumer
 * @see EntityManager#callWithConnection(ConnectionFunction)
 *
 * @since 3.2
 */
@FunctionalInterface
public interface ConnectionFunction<C,T> {
	/**
	 * Compute a result using the given connection.
	 *
	 * @param connection the connection to use
	 *
	 * @return the result
	 * 
	 * @throws Exception if a problem occurs calling the connection,
	 *                   usually a {@code java.sql.SQLException}
	 */
	T apply(C connection) throws Exception;
}
