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
//     Gavin King       - 3.2
//     Petros Splinakis - 2.2
//     Linda DeMichiel  - 2.1
//     Linda DeMichiel  - 2.0

package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a named native SQL query and, optionally, the mapping
 * of the result of the native SQL query. Query names are scoped
 * to the persistence unit. A named query may be executed by
 * calling {@link EntityManager#createNamedQuery(String, Class)}.
 *
 * <p> In simple cases, a {@link #resultClass} specifies how the
 * native SQL query result set should be interpreted, for example:
 * {@snippet :
 * @NamedNativeQuery(
 *         name = "findWidgets",
 *         query = "SELECT o.id, o.quantity, o.item " +
 *                 "FROM Order o, Item i " +
 *                 "WHERE (o.item = i.id) AND (i.name = 'widget')",
 *         resultClass = com.acme.Order.class
 * )}
 *
 * <p>
 * In more complicated cases, a {@linkplain SqlResultSetMapping
 * result set mapping} is needed, which may be specified using
 * either a separate annotation:
 * {@snippet :
 * @NamedNativeQuery(
 *         name = "OrderItems",
 *         query = "SELECT o.id, o.quantity, o.item, i.id, i.name, i.description " +
 *                 "FROM Order o, Item i " +
 *                 "WHERE (o.quantity > 25) AND (o.item = i.id)",
 *         resultSetMapping = "OrderItemResults"
 * )
 * @SqlResultSetMapping(name="OrderItemResults", entities={
 *     @EntityResult(entityClass=com.acme.Order.class),
 *     @EntityResult(entityClass=com.acme.Item.class)
 * })
 * }
 * or using the elements of this annotation:
 * {@snippet :
 * @NamedNativeQuery(
 *         name = "OrderItems",
 *         query = "SELECT o.id, o.quantity, o.item, i.id, i.name, i.description " +
 *                 "FROM Order o, Item i " +
 *                 "WHERE (o.quantity > 25) AND (o.item = i.id)",
 *         resultSetMapping = "OrderItemResults");
 *         entities={
 *                 @EntityResult(entityClass=com.acme.Order.class),
 *                 @EntityResult(entityClass=com.acme.Item.class)
 *         }
 * )
 * }
 *
 * <p> The {@code NamedNativeQuery} annotation can be applied to
 * an entity class or mapped superclass.
 * @see SqlResultSetMapping
 *
 * @since 1.0
 */
@Repeatable(NamedNativeQueries.class)
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface NamedNativeQuery { 

    /**
     * The name used to identify the query in calls to
     * {@link EntityManager#createNamedQuery}.
     */
    String name();

    /**
     * The native SQL query string.
     */
    String query();

    /**
     * Query properties and hints.
     * (May include vendor-specific query hints.)
     */
    QueryHint[] hints() default {};

    /**
     * The class of each query result. If a {@link #resultSetMapping
     * result set mapping} is specified, the specified result class
     * must agree with the type inferred from the result set mapping.
     * If a {@code resultClass} is not explicitly specified, then it
     * is inferred from the result set mapping, if any, or defaults
     * to {@code Object} or {@code Object[]}. The query result class
     * may be overridden by explicitly passing a class object to
     * {@link EntityManager#createNamedQuery(String, Class)}.
     */
    Class<?> resultClass() default void.class;

    /**
     * The name of a {@link SqlResultSetMapping}, as defined in metadata.
     * The named result set mapping is used to interpret the result set
     * of the native SQL query.
     *
     * <p>Alternatively, the elements {@link #entities}, {@link #classes},
     * and {@link #columns} may be used to specify a result set mapping.
     * These elements may not be used in conjunction with
     * {@code resultSetMapping}.
     */
    String resultSetMapping() default "";

    /**
     * Specifies the result set mapping to entities.
     * May not be used in combination with {@link #resultSetMapping}.
     * @since 3.2
     */
    EntityResult[] entities() default {};

    /**
     * Specifies the result set mapping to constructors.
     * May not be used in combination with {@link #resultSetMapping}.
     * @since 3.2
     */
    ConstructorResult[] classes() default {};

    /**
     * Specifies the result set mapping to scalar values.
     * May not be used in combination with {@link #resultSetMapping}.
     * @since 3.2
     */
    ColumnResult[] columns() default {};
}
