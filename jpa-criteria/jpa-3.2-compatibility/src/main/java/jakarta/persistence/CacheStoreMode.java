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
 * Specifies how the {@link EntityManager} interacts with the
 * second-level cache when data is read from the database and
 * when data is written to the database.
 * <ul>
 * <li>{@link #USE} indicates that data may be written to the
 *     second-level cache.
 * <li>{@link #BYPASS} indicates that data may not be written
 *     to the second-level cache.
 * <li>{@link #REFRESH} indicates that data must be written
 *     to the second-level cache, even when the data is already
 *     cached.
 * </ul>
 *
 * <p>Enumerates legal values of the property
 * {@code jakarta.persistence.cache.storeMode}.
 *
 * @see EntityManager#setCacheStoreMode(CacheStoreMode)
 * @see Query#setCacheStoreMode(CacheStoreMode)
 *
 * @since 2.0
 */
public enum CacheStoreMode implements FindOption, RefreshOption {

    /**
     * Insert entity data into cache when read from database and
     * insert/update entity data when written to the database:
     * this is the default behavior. Does not force refresh of
     * already cached items when reading from database.
     */
    USE,

    /**
     * Don't insert into cache. 
     */
    BYPASS,

    /**
     * Insert/update entity data held in the cache when read from
     * the database and when written to the database. Force refresh
     * of cache for items read from database.
     */
    REFRESH
}
