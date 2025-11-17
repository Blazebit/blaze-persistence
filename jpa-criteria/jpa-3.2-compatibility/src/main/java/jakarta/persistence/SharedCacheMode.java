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

import jakarta.persistence.spi.PersistenceUnitInfo;

/**
 * Specifies how the provider must use a second-level cache for the
 * persistence unit. Corresponds to the value of the {@code persistence.xml}
 * {@code shared-cache-mode} element, and returned as the result of
 * {@link PersistenceUnitInfo#getSharedCacheMode()}.
 * 
 * @since 2.0
 */
public enum SharedCacheMode {

    /**
     * All entities and entity-related state and data are cached.
     */
    ALL, 

    /**
     * Caching is disabled for the persistence unit.
     */
    NONE, 

    /**
     * Caching is enabled for all entities for which
     * {@link Cacheable Cacheable(true)} is specified. All other
     * entities are not cached.
     */
    ENABLE_SELECTIVE, 

    /**
     * Caching is enabled for all entities except those for which
     * {@link Cacheable Cacheable(false)} is specified. Entities
     * for which {@code Cacheable(false)} is specified are not cached.
     */
    DISABLE_SELECTIVE, 

    /**
     * Caching behavior is undefined: provider-specific defaults may apply.
     */
    UNSPECIFIED
}
