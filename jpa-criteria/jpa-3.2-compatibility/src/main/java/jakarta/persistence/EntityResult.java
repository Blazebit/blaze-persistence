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
//     Gavin King      - 3.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used in conjunction with the {@link SqlResultSetMapping} or
 * {@link NamedNativeQuery} annotation to map the SELECT clause
 * of a SQL query to an entity result.
 *
 * <p>If this annotation is used, the SQL statement should select 
 * all the columns that are mapped to the entity object.
 * This should include foreign key columns to related entities. 
 * The results obtained when insufficient data is available 
 * are undefined.
 *
 * <p>Example:
 * {@snippet :
 * Query q = em.createNativeQuery(
 *     "SELECT o.id, o.quantity, o.item, " +
 *         "i.id, i.name, i.description " +
 *       "FROM Order o, Item i " +
 *       "WHERE (o.quantity > 25) AND (o.item = i.id)",
 *     "OrderItemResults");
 *
 * @SqlResultSetMapping(
 *     name = "OrderItemResults",
 *     entities = {
 *         @EntityResult(entityClass = com.acme.Order.class),
 *         @EntityResult(entityClass = com.acme.Item.class)
 *     }
 * )
 * }
 *
 * @see SqlResultSetMapping
 * @see NamedNativeQuery
 *
 * @since 1.0
 */
@Target({}) 
@Retention(RUNTIME)
public @interface EntityResult { 

    /**
     * The class of the result.
     */
    Class<?> entityClass();

    /**
     * The lock mode obtained by the SQL query.
     * @since 3.2
     */
    LockModeType lockMode() default LockModeType.OPTIMISTIC;

    /** 
     * Maps the columns specified in the SELECT list of the 
     * query to the properties or fields of the entity class. 
     */
    FieldResult[] fields() default {};

    /** 
     * Specifies the column name (or alias) of the column in 
     * the SELECT list that is used to determine the type of 
     * the entity instance.
     */
    String discriminatorColumn() default "";
}
