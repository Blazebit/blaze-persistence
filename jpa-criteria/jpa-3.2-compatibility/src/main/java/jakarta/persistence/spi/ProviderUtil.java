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

package jakarta.persistence.spi;

import jakarta.persistence.FetchType;

/**
 * Utility interface implemented by the persistence provider. This
 * interface is invoked by the {@link jakarta.persistence.PersistenceUtil}
 * implementation to determine the load status of an entity or entity
 * attribute.
 *
 * @since 2.0
 */
public interface ProviderUtil { 

    /**
     * If the provider determines that the entity has been provided by
     * itself and that the state of the specified attribute has been loaded,
     * this method returns {@link LoadState#LOADED}.
     * <p> If the provider determines that the entity has been provided
     * by itself and that either entity attributes with {@link FetchType#EAGER}
     * have not been loaded or that the state of the specified attribute has
     * not been loaded, this method returns {@link LoadState#NOT_LOADED}.
     * <p> If a provider cannot determine the load state, this method
     * returns {@link LoadState#UNKNOWN}.
     * <p> The provider's implementation of this method must not obtain a
     * reference to an attribute value, as this could trigger the loading
     * of entity state if the entity has been provided by a different
     * provider.
     * @param entity  entity instance
     * @param attributeName  name of attribute whose load status is
     *        to be determined
     * @return load status of the attribute
     */
    LoadState isLoadedWithoutReference(Object entity, String attributeName);

    /**
     * If the provider determines that the entity has been provided by
     * itself and that the state of the specified attribute has been loaded,
     * this method returns {@link LoadState#LOADED}.
     * <p> If a provider determines that the entity has been provided by
     * itself and that either the entity attributes with {@link FetchType#EAGER}
     * have not been loaded or that the state of the specified attribute has
     * not been loaded, this method returns {@link LoadState#NOT_LOADED}.
     * <p> If the provider cannot determine the load state, this method
     * returns {@link LoadState#UNKNOWN}.
     * <p> The provider's implementation of this method is permitted to
     * obtain a reference to the attribute value. (This access is safe
     * because providers which might trigger the loading of the attribute
     * state will have already been determined by
     * {@link #isLoadedWithoutReference}.)
     *
     * @param entity  entity instance
     * @param attributeName  name of attribute whose load status is
     *        to be determined
     * @return load status of the attribute
     */
    LoadState isLoadedWithReference(Object entity, String attributeName);

    /**
     * If the provider determines that the entity has been provided by
     * itself and that the state of all attributes for which
     * {@link FetchType#EAGER} has been specified have been loaded, this
     * method returns {@link LoadState#LOADED}.
     * <p> If the provider determines that the entity has been provided
     * by itself and that not all attributes with {@link FetchType#EAGER}
     * have been loaded, this method returns {@link LoadState#NOT_LOADED}.
     * <p> If the provider cannot determine if the entity has been
     * provided by itself, this method returns {@link LoadState#UNKNOWN}.
     * <p> The provider's implementation of this method must not obtain
     * a reference to any attribute value, as this could trigger the
     * loading of entity state if the entity has been provided by a
     * different provider.
     * @param entity whose loaded status is to be determined
     * @return load status of the entity
     */
    LoadState isLoaded(Object entity);
}
