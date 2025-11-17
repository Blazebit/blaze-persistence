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

package jakarta.persistence.criteria;

/**
 * An object that defines an ordering over the query results.
 *
 * @since 2.0
 */
public interface Order {

   /**
    * Switch the ordering.
    * @return a new {@code Order} instance with the reversed ordering
    */
    Order reverse();

   /**
    * Whether ascending ordering is in effect.
    * @return boolean indicating whether ordering is ascending
    */
    boolean isAscending();

    /**
     * Return the precedence of null values.
     * @return the {@linkplain Nulls precedence of null values}
     * @since 3.2
     */
    Nulls getNullPrecedence();

   /**
    * Return the expression that is used for ordering.
    * @return expression used for ordering
    */
   Expression<?> getExpression();
}
