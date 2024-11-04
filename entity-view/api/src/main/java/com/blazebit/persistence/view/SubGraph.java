/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.view.metamodel.MethodPluralAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;

/**
 * Represents a fetch graph node for entity views.
 *
 * @param <T> The type of the fetched path
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface SubGraph<T> {

    /**
     * Adds a fetch for the given path and returns the {@link SubGraph} for it.
     * Careful, calling this method will cause that only the attribute paths that are added and reachable object ids will be fetched.
     * Other attributes will be fetched with their default value.
     *
     * @param <X>  The type of the fetched path
     * @param path The path to fetch
     * @return The sub graph for the path
     */
    public <X> SubGraph<X> fetch(String path);

    /**
     * Adds a fetch for the given attribute and returns the {@link SubGraph} for it.
     * Careful, calling this method will cause that only the attribute paths that are added and reachable object ids will be fetched.
     * Other attributes will be fetched with their default value.
     *
     * @param <X>  The type of the fetched path
     * @param attribute The attribute to fetch
     * @return The sub graph for the path
     * @since 1.5.0
     */
    public <X> SubGraph<X> fetch(MethodSingularAttribute<T, X> attribute);

    /**
     * Adds a fetch for the given attribute and returns the {@link SubGraph} for it.
     * Careful, calling this method will cause that only the attribute paths that are added and reachable object ids will be fetched.
     * Other attributes will be fetched with their default value.
     *
     * @param <X>  The type of the fetched path
     * @param attribute The attribute to fetch
     * @return The sub graph for the path
     * @since 1.5.0
     */
    public <X> SubGraph<X> fetch(MethodPluralAttribute<T, ?, X> attribute);

}
