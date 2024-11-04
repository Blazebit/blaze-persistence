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
 * Utility interface between the application and the persistence
 * provider(s). 
 * 
 * <p>The {@code PersistenceUtil} interface instance obtained from
 * the {@link Persistence} class is used to determine the load state
 * of an entity or entity attribute regardless of which persistence
 * provider in the environment created the entity.
 *
 * @since 2.0
 */
public interface PersistenceUtil {

    /**
     * Determine the load state of a given persistent attribute.
     * @param entity  entity containing the attribute
     * @param attributeName name of attribute whose load state is
     *        to be determined
     * @return false if entity's state has not been loaded or if
     * the attribute state has not been loaded, else true
     */
    boolean isLoaded(Object entity, String attributeName);

    /**
     * Determine the load state of an entity.
     * This method can be used to determine the load state of an
     * entity passed as a reference. An entity is considered loaded
     * if all attributes for which {@link FetchType#EAGER} has been
     * specified have been loaded.
     * <p>The {@link #isLoaded(Object, String)} method should be
     * used to determine the load state of an attribute. Not doing
     * so might lead to unintended loading of state.
     * @param entity whose load state is to be determined
     * @return false if the entity has not been loaded, else true
     */
    boolean isLoaded(Object entity);
}
