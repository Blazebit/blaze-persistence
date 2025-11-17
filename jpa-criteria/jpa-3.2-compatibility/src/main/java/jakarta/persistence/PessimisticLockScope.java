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
 *
 * Defines the values of the {@code jakarta.persistence.lock.scope}
 * property for pessimistic locking.  This property may be passed as an
 * argument to the methods of the {@link EntityManager}, {@link Query},
 * and {@link TypedQuery} interfaces that allow lock modes to be specified
 * or used with the {@link NamedQuery} annotation.
 *
 * @since 2.0
 */
public enum PessimisticLockScope implements FindOption, RefreshOption, LockOption {

    /**
     * This value defines the default behavior for pessimistic locking.
     *
     * <p>The persistence provider must lock the database row(s) that
     * correspond to the non-collection-valued persistent state of
     * that instance. If a joined inheritance strategy is used, or if
     * the entity is otherwise mapped to a secondary table, this
     * entails locking the row(s) for the entity instance in the
     * additional table(s). Entity relationships for which the locked
     * entity contains the foreign key will also be locked, but not
     * the state of the referenced entities (unless those entities are
     * explicitly locked). Element collections and relationships for
     * which the entity does not contain the foreign key (such as
     * relationships that are mapped to join tables or unidirectional
     * one-to-many relationships for which the target entity contains
     * the foreign key) will not be locked by default.
     */
    NORMAL,

    /**
     * In addition to the locking behavior specified for {@link #NORMAL},
     * element collections and relationships owned by the entity that
     * are contained in join tables are locked if the property
     * {@code jakarta.persistence.lock.scope} is specified with a value
     * of {@code PessimisticLockScope#EXTENDED}. The state of entities
     * referenced by such relationships is not locked (unless those
     * entities are explicitly locked). Locking such a relationship or
     * element collection generally locks only the rows in the join table
     * or collection table for that relationship or collection. This means
     * that phantoms are possible.
     */
    EXTENDED
}
