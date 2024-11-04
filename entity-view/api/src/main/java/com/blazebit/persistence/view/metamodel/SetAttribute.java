/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.CollectionInstantiator;
import com.blazebit.persistence.view.RecordingContainer;

import java.util.Set;

/**
 * Instances of the type {@linkplain SetAttribute} represent {@linkplain java.util.Set}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented Set
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SetAttribute<X, E> extends PluralAttribute<X, Set<E>, E> {

    /**
     * Returns the collection instantiator for this attribute.
     *
     * @param <R> The recording collection type
     * @return The collection instantiator
     * @since 1.5.0
     */
    public <R extends Set<E> & RecordingContainer<? extends Set<E>>> CollectionInstantiator<Set<E>, R> getCollectionInstantiator();
}
