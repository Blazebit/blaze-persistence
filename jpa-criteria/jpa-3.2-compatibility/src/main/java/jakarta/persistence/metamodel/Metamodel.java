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

package jakarta.persistence.metamodel;

import java.util.Set;

/**
 * Provides access to the metamodel of persistent entities in the
 * persistence unit.
 *
 * @since 2.0
 */
public interface Metamodel {

    /**
     * Return the metamodel entity type representing the entity.
     * @param entityName  the name of the represented entity
     * @return the metamodel entity type
     * @throws IllegalArgumentException if not an entity
     * @see jakarta.persistence.Entity#name
     * @since 3.2
     */
    EntityType<?> entity(String entityName);

    /**
     * Return the metamodel entity type representing the entity.
     * @param cls  the type of the represented entity
     * @return the metamodel entity type
     * @throws IllegalArgumentException if not an entity
     */
    <X> EntityType<X> entity(Class<X> cls);

    /**
     * Return the metamodel managed type representing the 
     * entity, mapped superclass, or embeddable class.
     * @param cls  the type of the represented managed class
     * @return the metamodel managed type
     * @throws IllegalArgumentException if not a managed class
     */
    <X> ManagedType<X> managedType(Class<X> cls);
    
    /**
     * Return the metamodel embeddable type representing the
     * embeddable class.
     * @param cls  the type of the represented embeddable class
     * @return the metamodel embeddable type
     * @throws IllegalArgumentException if not an embeddable class
     */
    <X> EmbeddableType<X> embeddable(Class<X> cls);

    /**
     * Return the metamodel managed types.
     * @return the metamodel managed types
     */
    Set<ManagedType<?>> getManagedTypes();

    /**
     * Return the metamodel entity types.
     * @return the metamodel entity types
     */
    Set<EntityType<?>> getEntities();

    /**
     * Return the metamodel embeddable types.
     * Returns am empty set if there are no embeddable types.
     * @return the metamodel embeddable types
     */
    Set<EmbeddableType<?>> getEmbeddables();
}
