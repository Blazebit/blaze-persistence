/*
 * Copyright (c) 2008, 2024 Oracle and/or its affiliates. All rights reserved.
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
//     Lukas Jungmann  - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

/**
 * Since blaze-persistence-core-impl implements this interface, but JPA 3.2 uses Java 17 bytecode,
 * the methods that need to be visible for compilation were added here, which compiles with Java 7 bytecode level.
 *
 * @since 1.6.17
 */
public interface TypedQuery<X> extends Query {

	@Override
    X getSingleResultOrNull();

	@Override
    TypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode);

	@Override
    TypedQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode);

	@Override
    TypedQuery<X> setTimeout(Integer timeout);
}
