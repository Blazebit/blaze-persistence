/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CollectionInstantiator;
import com.blazebit.persistence.view.RecordingContainer;

import java.util.List;

/**
 * Instances of the type {@linkplain ListAttribute} represent {@linkplain java.util.List}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented List
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ListAttribute<X, E> extends PluralAttribute<X, List<E>, E> {

    /**
     * Returns the collection instantiator for this attribute.
     *
     * @param <R> The recording collection type
     * @return The collection instantiator
     * @since 1.5.0
     */
    public <R extends List<E> & RecordingContainer<? extends List<E>>> CollectionInstantiator<List<E>, R> getCollectionInstantiator();

    /**
     * Returns the index mapping of the attribute.
     *
     * @return The index mapping of the attribute
     * @since 1.5.0
     */
    public String getIndexMapping();

    /**
     * Renders the index mapping for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderIndexMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb);
}
