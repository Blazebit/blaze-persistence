/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.util.Set;

/**
 * An interface for builders that support access to the from elements.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface FromProvider {

    /**
     * Returns the query roots.
     *
     * @return The roots of this query
     * @since 1.2.0
     */
    public Set<From> getRoots();

    /**
     * Returns the from element for the given alias or null.
     *
     * @param alias The alias of the from element
     * @return The from element of this query or null if not found
     * @since 1.2.0
     */
    public From getFrom(String alias);

    /**
     * Returns the from element for the given path, creating it if necessary.
     *
     * @param path The path to the from element
     * @return The from element of this query
     * @since 1.2.0
     */
    public From getFromByPath(String path);

    /**
     * Returns the path object for the given path string, creating it if necessary.
     *
     * @param path The path string
     * @return The path object for this query
     * @since 1.2.1
     */
    public Path getPath(String path);

}
