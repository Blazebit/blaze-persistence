/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
