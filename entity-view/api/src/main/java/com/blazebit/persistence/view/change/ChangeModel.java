/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.change;

/**
 * An interface for accessing the change model of an object.
 *
 * @param <E> The element type represented by the change model
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ChangeModel<E> {

    /**
     * Returns the initial state of the change model.
     *
     * @return The initial state
     */
    public E getInitialState();

    /**
     * Returns the current state of the change model.
     *
     * @return The current state
     */
    public E getCurrentState();

    /**
     * Returns the kind of the change done to the attribute.
     *
     * @return The change kind
     */
    public ChangeKind getKind();

    /**
     * The kind of a change model.
     * {@linkplain #UPDATED} indicates a reference change,
     * whereas {@linkplain #MUTATED} indicates a nested change.
     */
    enum ChangeKind {
        /**
         * Indicates nothing changed.
         */
        NONE,
        /**
         * Indicates a reference changed.
         */
        UPDATED,
        /**
         * Indicates a nested change.
         */
        MUTATED;
    }

    /**
     * Returns whether the object is dirty/was modified.
     *
     * @return True if dirty, false otherwise
     */
    public boolean isDirty();
}
