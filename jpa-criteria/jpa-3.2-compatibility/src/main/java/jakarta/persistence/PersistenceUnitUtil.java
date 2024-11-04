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

import jakarta.persistence.metamodel.Attribute;

/**
 * Utility interface between the application and the persistence
 * provider managing the persistence unit.
 *
 * <p>The methods of this interface should only be invoked on
 * entity instances obtained from or managed by entity managers
 * for this persistence unit or on new entity instances.
 *
 * @since 2.0
 */
public interface PersistenceUnitUtil extends PersistenceUtil {

    /**
     * Determine the load state of a given persistent attribute
     * of an entity belonging to the persistence unit.
     * @param entity  entity instance containing the attribute
     * @param attributeName name of attribute whose load state is
     *        to be determined
     * @return false if entity's state has not been loaded or if 
     *         the attribute state has not been loaded, else true
     */
    boolean isLoaded(Object entity, String attributeName);

    /**
     * Determine the load state of a given persistent attribute
     * of an entity belonging to the persistence unit.
     * @param entity  entity instance containing the attribute
     * @param attribute  attribute whose load state is to be determined
     * @return false if entity's state has not been loaded or if
     *         the attribute state has not been loaded, else true
     * @since 3.2
     */
    <E> boolean isLoaded(E entity, Attribute<? super E, ?> attribute);

    /**
     * Determine the load state of an entity belonging to the
     * persistence unit. This method can be used to determine the
     * load state of an entity passed as a reference. An entity is
     * considered loaded if all attributes for which
     * {@link FetchType#EAGER} has been specified have been loaded.
     * <p> The {@link #isLoaded(Object, String)} method should be
     * used to determine the load state of an attribute. Not doing
     * so might lead to unintended loading of state.
     * @param entity   entity instance whose load state is to be determined
     * @return false if the entity has not been loaded, else true
     */
    boolean isLoaded(Object entity);

    /**
     * Load the persistent value of a given persistent attribute
     * of an entity belonging to the persistence unit and to an
     * open persistence context.
     * After this method returns, {@link #isLoaded(Object,String)}
     * must return true with the given entity instance and attribute.
     * @param entity  entity instance
     * @param attributeName  the name of the attribute to be loaded
     * @throws IllegalArgumentException if the given object is not an
     * instance of an entity class belonging to the persistence unit
     * @throws PersistenceException if the entity is not associated
     * with an open persistence context or cannot be loaded from the
     * database
     * @since 3.2
     */
    void load(Object entity, String attributeName);

    /**
     * Load the persistent value of a given persistent attribute
     * of an entity belonging to the persistence unit and to an
     * open persistence context.
     * After this method returns, {@link #isLoaded(Object,Attribute)}
     * must return true with the given entity instance and attribute.
     * @param entity  entity instance to be loaded
     * @param attribute  the attribute to be loaded
     * @throws IllegalArgumentException if the given object is not an
     * instance of an entity class belonging to the persistence unit
     * @throws PersistenceException if the entity is not associated
     * with an open persistence context or cannot be loaded from the
     * database
     * @since 3.2
     */
    <E> void load(E entity, Attribute<? super E, ?> attribute);

    /**
     * Load the persistent state of an entity belonging to the
     * persistence unit and to an open persistence context.
     * After this method returns, {@link #isLoaded(Object)} must
     * return true with the given entity instance.
     * @param entity  entity instance to be loaded
     * @throws IllegalArgumentException if the given object is not an
     * instance of an entity class belonging to the persistence unit
     * @throws PersistenceException if the entity is not associated
     * with an open persistence context or cannot be loaded from the
     * database
     * @since 3.2
     */
    void load(Object entity);

    /**
     * Return true if the given entity belonging to the persistence
     * unit and to an open persistence context is an instance of the
     * given entity class, or false otherwise. This method may, but
     * is not required to, load the given entity by side effect.
     * @param entity  entity instance
     * @param entityClass  an entity class belonging to the persistence
     * unit
     * @throws IllegalArgumentException if the given object is not an
     * instance of an entity class belonging to the persistence unit
     * or if the given class is not an entity class belonging to the
     * persistence unit
     * @throws PersistenceException if the entity is not associated
     * with an open persistence context or cannot be loaded from the
     * database
     * @since 3.2
     */
    boolean isInstance(Object entity, Class<?> entityClass);

    /**
     * Return the concrete entity class if the given entity belonging
     * to the persistence unit and to an open persistence context.
     * This method may, but is not required to, load the given entity
     * by side effect.
     * @param entity  entity instance
     * @return an entity class belonging to the persistence unit
     * @throws IllegalArgumentException if the given object is not an
     * instance of an entity class belonging to the persistence unit
     * @throws PersistenceException if the entity is not associated
     * with an open persistence context or cannot be loaded from the
     * database
     * @since 3.2
     */
    <T> Class<? extends T> getClass(T entity);

    /**
     * Return the id of the entity.
     * A generated id is not guaranteed to be available until after
     * the database insert has occurred.
     * Returns null if the entity does not yet have an id.
     * @param entity  entity instance
     * @return id of the entity
     * @throws IllegalArgumentException if the object is found not 
     *         to be an entity
     */
    Object getIdentifier(Object entity);

    /**
     * Return the version of the entity.
     * A generated version is not guaranteed to be available until after
     * the database insert has occurred.
     * Returns null if the entity does not yet have an id.
     * @param entity  entity instance
     * @return id of the entity
     * @throws IllegalArgumentException if the object is found not
     *         to be an entity
     * @since 3.2
     */
    Object getVersion(Object entity);
}
