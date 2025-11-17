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
 * Used in conjunction with the {@link EntityResult} annotation to map
 * columns specified in the SELECT list of a SQL query to the properties
 * or fields of an entity class.
 *
 *
 * <p>Example:
 * {@snippet :
 * Query q = em.createNativeQuery(
 *     "SELECT o.id AS order_id, " +
 *         "o.quantity AS order_quantity, " +
 *         "o.item AS order_item, " +
 *       "FROM Order o, Item i " +
 *       "WHERE (order_quantity > 25) AND (order_item = i.id)",
 *     "OrderResults");
 *
 * @SqlResultSetMapping(
 *     name = "OrderResults",
 *     entities = {
 *         @EntityResult(
 *             entityClass = com.acme.Order.class,
 *             fields = {
 *                 @FieldResult(name = "id", column = "order_id"),
 *                 @FieldResult(name = "quantity", column = "order_quantity"),
 *                 @FieldResult(name = "item", column = "order_item")
 *             })
 *     })
 * }
 *
 * @see EntityResult
 * @see SqlResultSetMapping
 * @since 1.0
 */
@Target({}) 
@Retention(RUNTIME)
public @interface FieldResult { 

    /**
     * Name of the persistent field or property of the class.
     */
    String name();

    /** 
     * Name of the column in the SELECT clause - i.e., column 
     * aliases, if applicable. 
     */
    String column();
}
