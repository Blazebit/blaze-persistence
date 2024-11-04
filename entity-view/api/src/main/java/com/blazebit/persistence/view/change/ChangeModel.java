/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
