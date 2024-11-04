/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The cascade types for updatable entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum CascadeType {

    /**
     * Defines that {@link #PERSIST} and/or {@link #UPDATE} cascading should be applied
     * based on the element type and the availability of a setter.
     * Whether {@link #DELETE} cascading is activated is determined based on the entity mapping.
     */
    AUTO,
    /**
     * Defines that <em>new</em> elements should be persisted.
     */
    PERSIST,
    /**
     * Defines that <em>existing</em> elements should be updated.
     */
    UPDATE,
    /**
     * Defines that when the declaring type of an attribute is deleted, elements of the attribute are deleted as well.
     * Note that this cascading type is redundant if {@link UpdatableMapping#orphanRemoval()} is active for the attribute
     * or when the attribute is an inverse attribute. The {@link MappingInverse#removeStrategy()} defines <em>how</em> the deletion is done.
     */
    DELETE;
}
