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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used in conjunction with the {@link SqlResultSetMapping} or
 * {@link NamedNativeQuery} annotation to map the SELECT clause
 * of a SQL query to a constructor.
 *
 * <p>Applies a constructor for the target class, passing in as
 * arguments values from the specified columns. All columns
 * corresponding to arguments of the intended constructor must
 * be specified using the {@link #columns} element of the
 * {@code ConstructorResult} annotation in the same order as that
 * of the argument list of the constructor. Any entities returned
 * as constructor results will be in either the new or detached
 * state, depending on whether a primary key is retrieved for
 * the constructed object.
 *
 * <p>Example:
 * {@snippet :
 * Query q = em.createNativeQuery(
 *     "SELECT c.id, c.name, " +
 *         "COUNT(o) as orderCount, " +
 *         "AVG(o.price) AS avgOrder " +
 *       "FROM Customer c, Orders o " +
 *       "WHERE o.cid = c.id " +
 *       "GROUP BY c.id, c.name",
 *     "CustomerDetailsResult");
 *
 * @SqlResultSetMapping(
 *     name = "CustomerDetailsResult",
 *     classes = {
 *         @ConstructorResult(
 *             targetClass = com.acme.CustomerDetails.class,
 *             columns = {
 *                 @ColumnResult(name = "id"),
 *                 @ColumnResult(name = "name"),
 *                 @ColumnResult(name = "orderCount"),
 *                 @ColumnResult(name = "avgOrder", type = Double.class)
 *             })
 *     })
 * }
 *
 * @see SqlResultSetMapping
 * @see NamedNativeQuery
 * @see ColumnResult
 *
 * @since 2.1
 */
@Target({}) 
@Retention(RUNTIME)
public @interface ConstructorResult { 

    /**
     * (Required) The class whose constructor is to be invoked.
     */
    Class<?> targetClass();

    /** 
     * (Required) The mapping of columns in the SELECT list
     * to the arguments of the intended constructor, in order.
     */
    ColumnResult[] columns();
}
