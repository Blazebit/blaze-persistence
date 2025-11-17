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
 * Declares a named query written in the Jakarta Persistence
 * query language. Query names are scoped to the persistence unit.
 * A named query may be executed by calling
 * {@link EntityManager#createNamedQuery(String, Class)}.
 *
 * <p> The following is an example of the definition of a named
 * query written in the Jakarta Persistence query language:
 * {@snippet :
 * @NamedQuery(
 *     name = "findAllCustomersWithName",
 *     query = "SELECT c FROM Customer c WHERE c.name LIKE :custName")
 * }
 *
 * <p> The named query may be executed like this:
 * {@snippet :
 * @PersistenceContext EntityManager em;
 * ...
 * List<Customer> customers = em.createNamedQuery("findAllCustomersWithName", Customer.class)
 *               .setParameter("custName", "Smith")
 *               .getResultList();
 * }
 *
 * The {@code NamedQuery} annotation can be applied to an entity
 * class or mapped superclass.
 *
 * @since 1.0
 */
@Repeatable(NamedQueries.class)
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface NamedQuery {

    /** 
     * (Required) The name used to identify the query in calls to
     * {@link EntityManager#createNamedQuery}.
     */
    String name();

    /**
     * (Required) The query string in the Jakarta Persistence
     * query language.
     */
    String query();

    /**
     * (Optional) The class of each query result. The result class
     * may be overridden by explicitly passing a class object to
     * {@link EntityManager#createNamedQuery(String, Class)}. If
     * the result class of a named query is not specified, the
     * persistence implementation is entitled to default the
     * result class to {@code Object} or {@code Object[]}.
     */
    Class<?> resultClass() default void.class;

    /**
     * (Optional) The lock mode type to use in query execution.
     * If a {@code lockMode} other than {@link LockModeType#NONE}
     * is specified, the query must be executed in a transaction
     * and the persistence context joined to the transaction.
     * @since 2.0
     */
    LockModeType lockMode() default LockModeType.NONE;
    
    /**
     * (Optional) Query properties and hints. May include
     * vendor-specific query hints.
     */
    QueryHint[] hints() default {};
}
