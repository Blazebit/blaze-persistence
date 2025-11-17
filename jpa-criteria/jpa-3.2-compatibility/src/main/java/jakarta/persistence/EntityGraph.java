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
 * This type represents the root of an entity graph that will be
 * used as a template to define the attribute nodes and boundaries
 * of a graph of entities and entity relationships. The root must
 * be an entity type.
 * <p>
 * The methods to add subgraphs implicitly create the corresponding
 * attribute nodes as well; such attribute nodes should not be
 * redundantly specified.
 *
 * @param <T> The type of the root entity.
 *
 * @see AttributeNode
 * @see Subgraph
 * @see NamedEntityGraph
 *
 * @see EntityManager#createEntityGraph(Class)
 * @see EntityManager#createEntityGraph(String)
 * @see EntityManager#getEntityGraph(String)
 * @see EntityManagerFactory#addNamedEntityGraph(String, EntityGraph)
 * @see EntityManager#find(EntityGraph, Object, FindOption...)
 *
 * @since 2.1
 */
public interface EntityGraph<T> extends Graph<T> {

    /**
     * Return the name of a named {@code EntityGraph} (an entity
     * graph defined by means of the {@link NamedEntityGraph}
     * annotation, XML descriptor element, or added by means of the
     * {@link EntityManagerFactory#addNamedEntityGraph} method).
     * Returns null if the {@code EntityGraph} is not a named
     * {@code EntityGraph}.
     */
    String getName();

    /**
     * Add additional attributes to this entity graph that
     * correspond to attributes of subclasses of the entity type of
     * this {@code EntityGraph}. Subclass subgraphs automatically
     * include the specified attributes of superclass subgraphs.
     *
     * @param type  entity subclass
     * @return subgraph for the subclass
     * @throws IllegalArgumentException if the type is not an entity type
     * @throws IllegalStateException if the EntityGraph has been
     *         statically defined
     */
    <S extends T> Subgraph<S> addTreatedSubgraph(Class<S> type);

    /**
     * Add additional attributes to this entity graph that
     * correspond to attributes of subclasses of the entity type of
     * this {@code EntityGraph}. Subclass subgraphs automatically
     * include the specified attributes of superclass subgraphs.
     *
     * @param type  entity subclass
     * @return subgraph for the subclass
     * @throws IllegalArgumentException if the type is not an entity type
     * @throws IllegalStateException if the EntityGraph has been 
     *         statically defined
     * @deprecated use {@link #addTreatedSubgraph(Class)}
     */
    @Deprecated(since = "3.2", forRemoval = true)
    <T> Subgraph<? extends T> addSubclassSubgraph(Class<? extends T> type);

}
