/*
 * Copyright (c) 2011, 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

/**
 * This type represents a subgraph for an attribute node that
 * corresponds to a managed type. Using this class, an entity
 * subgraph can be embedded within an {@link EntityGraph}.
 *
 * @param <T> The type of the attribute.
 *
 * @see EntityGraph
 * @see AttributeNode
 * @see NamedSubgraph
 *
 * @since 2.1
 */
public interface Subgraph<T> extends Graph<T> {

    /**
     * Return the type for which this subgraph was defined.
     * @return managed type referenced by the subgraph
     */
    Class<T> getClassType();

}
