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
 * Interface used to interact with the second-level cache.
 * If no second-level cache is in use, the methods of this
 * interface have no effect, except for {@link #contains},
 * which returns false.
 *
 * @since 2.0
 */
public interface Cache {

    /**
     * Whether the cache contains data for the given entity.
     * @param cls  entity class 
     * @param primaryKey  primary key
     * @return boolean indicating whether the entity is in the cache
     */
    boolean contains(Class<?> cls, Object primaryKey);

    /**
     * Remove the data for the given entity from the cache.
     * @param cls  entity class
     * @param primaryKey  primary key
     */
    void evict(Class<?> cls, Object primaryKey);

    /**
     * Remove the data for entities of the specified class
     * (and its subclasses) from the cache.
     * @param cls  entity class
     */
    void evict(Class<?> cls);

    /**
     * Clear the cache.
     */
    void evictAll();

    /**
     * Return an object of the specified type to allow access to
     * the provider-specific API. If the provider's implementation
     * of the {@code Cache} interface does not support the specified
     * class, the {@link PersistenceException} is thrown.
     * @param cls  the class of the object to be returned.
     *             This is usually either the underlying class
     *             implementing {@code Cache}, or an interface it
     *             implements.
     * @return an instance of the specified type
     * @throws PersistenceException if the provider does not support
     *         the given type
     * @since 2.1
     */
    <T> T unwrap(Class<T> cls);
}
