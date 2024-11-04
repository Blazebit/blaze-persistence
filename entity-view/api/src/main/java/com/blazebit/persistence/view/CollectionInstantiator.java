/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.util.Collection;

/**
 * An instantiator for normal, recording and JPA collections for an entity view attribute.
 *
 * @param <C> The collection type
 * @param <R> The recording container type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface CollectionInstantiator<C extends Collection<?>, R extends Collection<?> & RecordingContainer<? extends C>> {

    /**
     * Creates a plain collection.
     *
     * @param size The size estimate
     * @return the collection
     */
    public C createCollection(int size);

    /**
     * Creates a recording collection.
     *
     * @param size The size estimate
     * @return the recording collection
     */
    public R createRecordingCollection(int size);
}