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
//     Petros Splinakis - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * Specifies a mapping of the columns of a result set of a native SQL query
 * or stored procedure.
 *
 * <p>Example:
 * {@snippet :
 * Query q = em.createNativeQuery(
 *     "SELECT o.id AS order_id, " +
 *         "o.quantity AS order_quantity, " +
 *         "o.item AS order_item, " +
 *         "i.name AS item_name, " +
 *       "FROM Order o, Item i " +
 *       "WHERE (order_quantity &gt; 25) AND (order_item = i.id)",
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
 *     },
 *     columns = {
 *         @ColumnResult(name = "item_name")
 *     })
 * }
 *
 * @see Query
 * @see StoredProcedureQuery
 * @see NamedNativeQuery
 * @see NamedStoredProcedureQuery
 *
 * @since 1.0
 */
@Repeatable(SqlResultSetMappings.class)
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface SqlResultSetMapping { 

    /** 
     * The name given to the result set mapping, and used to
     * refer to it in the methods of the {@link Query} and
     * {@link StoredProcedureQuery} APIs.
     */
    String name(); 

    /**
     * Specifies the result set mapping to entities.
     */
    EntityResult[] entities() default {};

    /** 
     * Specifies the result set mapping to constructors. 
     * @since 2.1
     */
    ConstructorResult[] classes() default {};

    /**
     * Specifies the result set mapping to scalar values.
     */
    ColumnResult[] columns() default {};
}
