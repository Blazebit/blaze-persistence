/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.CollectionInstantiator;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CollectionInstantiatorImplementor<C extends Collection<?>, R extends RecordingCollection<? extends C, ?>> extends CollectionInstantiator<C, R> {

    /**
     * Returns whether the collection type allows duplicates.
     *
     * @return whether the collection type allows duplicates
     */
    public boolean allowsDuplicates();

    /**
     * Returns whether the collection type is indexed.
     *
     * @return whether the collection type is indexed
     */
    public boolean isIndexed();

    /**
     * Returns whether the collection requires a post construct call.
     *
     * @return whether the collection requires a post construct call
     */
    public boolean requiresPostConstruct();

    /**
     * Invokes the post construct action on the collection.
     *
     * @param collection The collection
     */
    public void postConstruct(Collection<?> collection);

    /**
     * Creates a collection for the JPA model.
     *
     * @param size The size estimate
     * @return the collection
     */
    public Collection<?> createJpaCollection(int size);
}
