/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

import java.util.Map;

/**
 * A reference to a named query declared via the {@link NamedQuery}
 * or {@link NamedNativeQuery} annotations.
 *
 * @param <R> an upper bound on the result type of the query
 *
 * @see EntityManager#createQuery(TypedQueryReference)
 *
 * @since 3.2
 */
public interface TypedQueryReference<R> {
    /**
     * The name of the query.
     */
    String getName();

    /**
     * The result type of the query.
     */
    Class<? extends R> getResultType();

    /**
     * A map keyed by hint name of all hints specified via
     * {@link NamedQuery#hints} or {@link NamedNativeQuery#hints}.
     */
    Map<String,Object> getHints();
}
